/*******************************************************************************
 *
 * Copyright (c) 2016 ecFeed AS.                                                
 * All rights reserved. This program and the accompanying materials              
 * are made available under the terms of the Eclipse Public License v1.0         
 * which accompanies this distribution, and is available at                      
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 *******************************************************************************/

package com.ecfeed.core.model;

import java.util.List;

import com.ecfeed.core.utils.ExceptionHelper;
import com.ecfeed.core.utils.JavaTypeHelper;
import com.ecfeed.core.utils.ObjectHelper;

public class ChoiceCondition implements IStatementCondition {

	private ChoiceNode fRightChoice;
	RelationStatement fParentRelationStatement;

	public ChoiceCondition(ChoiceNode rightChoice, RelationStatement parentRelationStatement) {

		fRightChoice = rightChoice;
		fParentRelationStatement = parentRelationStatement;
	}

	@Override
	public boolean evaluate(List<ChoiceNode> choices) {

		ChoiceNode choice = StatementConditionHelper.getChoiceForMethodParameter(choices, fParentRelationStatement.getLeftParameter());

		return evaluateChoice(choice);
	}

	@Override
	public boolean adapt(List<ChoiceNode> values) {
		return false;
	}

	@Override
	public ChoiceCondition getCopy() {
		return new ChoiceCondition(fRightChoice.makeClone(), fParentRelationStatement);
	}

	@Override
	public boolean updateReferences(MethodNode methodNode) {

		String parameterName = fParentRelationStatement.getLeftParameter().getName();
		MethodParameterNode methodParameterNode = methodNode.getMethodParameter(parameterName);

		String choiceName = fRightChoice.getQualifiedName();
		ChoiceNode choiceNode = methodParameterNode.getChoice(choiceName);

		if (choiceNode == null) {
			return false;
		}
		fRightChoice = choiceNode;

		return true;
	}

	@Override
	public Object getCondition(){
		return fRightChoice;
	}

	@Override
	public boolean compare(IStatementCondition condition) {

		if (condition instanceof ChoiceCondition == false) {
			return false;
		}

		ChoiceCondition compared = (ChoiceCondition)condition;

		return (fRightChoice.isMatch((ChoiceNode)compared.getCondition()));
	}

	@Override
	public Object accept(IStatementVisitor visitor) throws Exception {
		return visitor.visit(this);
	}

	@Override
	public String toString() {

		return StatementConditionHelper.createChoiceDescription(fRightChoice.getQualifiedName());
	}

	@Override
	public boolean mentions(MethodParameterNode methodParameterNode) {

		return false;
	}	

	public ChoiceNode getRightChoice() {
		return fRightChoice;
	}

	private boolean evaluateChoice(ChoiceNode actualChoice) {

		EStatementRelation relation = fParentRelationStatement.getRelation();
		if (relation == EStatementRelation.EQUAL || relation == EStatementRelation.NOT_EQUAL) {
			return evaluateEqualityIncludingParents(relation, actualChoice);
		}

		String typeName = actualChoice.getParameter().getType();

		String actualValue = JavaTypeHelper.convertValueString(actualChoice.getValueString(), typeName);
		String valueToMatch = JavaTypeHelper.convertValueString(fRightChoice.getValueString(), typeName);

		return StatementConditionHelper.isRelationMatchQuiet(relation, typeName, actualValue, valueToMatch);
	}

	private boolean evaluateEqualityIncludingParents(EStatementRelation relation, ChoiceNode choice) {

		boolean isMatch = false;

		if (choice == null || fRightChoice == null) {
			isMatch = ObjectHelper.isEqualWhenOneOrTwoNulls(choice, fRightChoice);
		} else {
			isMatch = choice.isMatchIncludingParents(fRightChoice);
		}

		switch (relation) {

		case EQUAL:
			return isMatch;
		case NOT_EQUAL:
			return !isMatch;
		default:
			ExceptionHelper.reportRuntimeException("Invalid relation.");
			return false;
		}
	}

}

