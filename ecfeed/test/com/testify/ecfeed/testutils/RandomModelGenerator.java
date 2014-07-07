package com.testify.ecfeed.testutils;

import static com.testify.ecfeed.model.Constants.REGEX_CATEGORY_NODE_NAME;
import static com.testify.ecfeed.model.Constants.REGEX_CATEGORY_TYPE_NAME;
import static com.testify.ecfeed.model.Constants.REGEX_CLASS_NODE_NAME;
import static com.testify.ecfeed.model.Constants.REGEX_CONSTRAINT_NODE_NAME;
import static com.testify.ecfeed.model.Constants.REGEX_METHOD_NODE_NAME;
import static com.testify.ecfeed.model.Constants.REGEX_PARTITION_LABEL;
import static com.testify.ecfeed.model.Constants.REGEX_PARTITION_NODE_NAME;
import static com.testify.ecfeed.model.Constants.REGEX_ROOT_NODE_NAME;
import static com.testify.ecfeed.model.Constants.REGEX_STRING_TYPE_VALUE;
import static com.testify.ecfeed.model.Constants.REGEX_USER_TYPE_VALUE;
import static com.testify.ecfeed.model.Constants.TYPE_NAME_BOOLEAN;
import static com.testify.ecfeed.model.Constants.TYPE_NAME_BYTE;
import static com.testify.ecfeed.model.Constants.TYPE_NAME_CHAR;
import static com.testify.ecfeed.model.Constants.TYPE_NAME_DOUBLE;
import static com.testify.ecfeed.model.Constants.TYPE_NAME_FLOAT;
import static com.testify.ecfeed.model.Constants.TYPE_NAME_INT;
import static com.testify.ecfeed.model.Constants.TYPE_NAME_LONG;
import static com.testify.ecfeed.model.Constants.TYPE_NAME_SHORT;
import static com.testify.ecfeed.model.Constants.TYPE_NAME_STRING;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import nl.flotsam.xeger.Xeger;

import com.testify.ecfeed.model.CategoryNode;
import com.testify.ecfeed.model.ClassNode;
import com.testify.ecfeed.model.ConstraintNode;
import com.testify.ecfeed.model.MethodNode;
import com.testify.ecfeed.model.PartitionNode;
import com.testify.ecfeed.model.RootNode;
import com.testify.ecfeed.model.constraint.BasicStatement;
import com.testify.ecfeed.model.constraint.Constraint;
import com.testify.ecfeed.model.constraint.ExpectedValueStatement;
import com.testify.ecfeed.model.constraint.Operator;
import com.testify.ecfeed.model.constraint.PartitionedCategoryStatement;
import com.testify.ecfeed.model.constraint.Relation;
import com.testify.ecfeed.model.constraint.StatementArray;
import com.testify.ecfeed.model.constraint.StaticStatement;

public class RandomModelGenerator {
	
	private Random rand = new Random();
	
	public int MAX_CLASSES = 10;
	public int MAX_METHODS = 10;
	public int MAX_CATEGORIES = 10;
	public int MAX_CONSTRAINTS = 10;
	public int MAX_PARTITIONS = 10;
	public int MAX_PARTITION_LEVELS = 5;
	public int MAX_PARTITION_LABELS = 10;
	public int MAX_STATEMENTS = 5;
	
	
	private final String USER_TYPE = "user.type";
	private final String[] SUPPORTED_TYPES = {
			TYPE_NAME_BOOLEAN,
			TYPE_NAME_BYTE,
			TYPE_NAME_CHAR,
			TYPE_NAME_DOUBLE,
			TYPE_NAME_FLOAT,
			TYPE_NAME_INT,
			TYPE_NAME_LONG,
			TYPE_NAME_SHORT,
			TYPE_NAME_STRING,
			USER_TYPE
	};

	
	public RootNode generateModel(){
		String name = generateString(REGEX_ROOT_NODE_NAME);
		
		RootNode root = new RootNode(name);
		
		for(int i = 0; i < rand.nextInt(MAX_CLASSES) + 1; i++){
			root.addClass(generateClass());
		}
		
		return root;
	}

	
	
	public ClassNode generateClass() {
		String name = generateString(REGEX_CLASS_NODE_NAME);

		ClassNode _class = new ClassNode(name);
		
		for(int i = 0; i < rand.nextInt(MAX_METHODS) + 1; i++){
			_class.addMethod(generateMethod());
		}

		return _class;
	}

	public MethodNode generateMethod(){
		String name = generateString(REGEX_METHOD_NODE_NAME);
		
		MethodNode method = new MethodNode(name);
		
		for(int i = 0; i < MAX_CATEGORIES; i++){
			method.addCategory(generateCategory());
		}
		
		for(int i = 0; i < MAX_CONSTRAINTS; i++){
			method.addConstraint(generateConstraint(method));
		}
		
		return method;
	}
	
	public CategoryNode generateCategory(){
		String name = generateString(REGEX_CATEGORY_NODE_NAME);
		String type = randomType();
		boolean expected = rand.nextInt(4) < 3 ? false : true;
		
		CategoryNode category = new CategoryNode(name, type, expected);
		category.setDefaultValueString(randomPartitionValue(type));
		
		for(int i = 0; i < rand.nextInt(MAX_PARTITIONS); i++){
			int levels = rand.nextInt(MAX_PARTITION_LEVELS);
			category.addPartition(generatePartition(levels, MAX_PARTITIONS, MAX_PARTITION_LABELS, type));
		}
		
		return category;
	}

	public ConstraintNode generateConstraint(MethodNode method){
		String name = generateString(REGEX_CONSTRAINT_NODE_NAME);
		
		Constraint constraint = new Constraint(generatePremise(method), generateConsequence(method));
		
		return new ConstraintNode(name, constraint);
	}
	
	public BasicStatement generatePremise(MethodNode method) {
		return generateStatement(method);
	}

	public BasicStatement generateStatement(MethodNode method) {
		switch(rand.nextInt(3)){
		case 0: return new StaticStatement(rand.nextBoolean());
		case 1: return generatePartitionedStatement(method);
		default: return generateStatementArray(method); 
		}
	}

	public BasicStatement generatePartitionedStatement(MethodNode method) {
		List<CategoryNode> categories = new ArrayList<CategoryNode>();
		
		for(CategoryNode category : method.getCategories()){
			if(category.isExpected() == false && category.getPartitions().size() > 0){
				categories.add(category);
			}
		}
		
		if(categories.size() == 0){
			return new StaticStatement(true);
		}
		
		CategoryNode category = categories.get(categories.size());
		Relation relation = rand.nextBoolean() ? Relation.EQUAL : Relation.NOT;
		
		if(rand.nextBoolean()){
			List<String> partitionNames = category.getAllPartitionNames();
			String luckyPartitionName = partitionNames.get(rand.nextInt(partitionNames.size()));
			PartitionNode condition = category.getPartition(luckyPartitionName);
			return new PartitionedCategoryStatement(category, relation, condition);
		}
		else{
			Set<String>labels = category.getAllPartitionLabels();
			String label = labels.toArray(new String[]{})[rand.nextInt(labels.size())];
			return new PartitionedCategoryStatement(category, relation, label);
		}
	}

	public BasicStatement generateStatementArray(MethodNode method) {
		StatementArray statement = new StatementArray(rand.nextBoolean()?Operator.AND:Operator.OR);
		for(int i = 0; i < MAX_STATEMENTS; i++){
			statement.addStatement(generateStatement(method));
		}
		return statement;
	}

	public BasicStatement generateConsequence(MethodNode method) {
		List<CategoryNode> categories = method.getCategories();
		CategoryNode category = categories.get(rand.nextInt(categories.size()));
		if(category.isExpected()){
			return generateExpectedValueStatement(category);
		}
		return generateStatement(method);
	}

	public BasicStatement generateExpectedValueStatement(CategoryNode category) {
		String value = randomPartitionValue(category.getType());
		String name = generateString(REGEX_PARTITION_NODE_NAME);
		return new ExpectedValueStatement(category, new PartitionNode(name, value));
	}

	public PartitionNode generatePartition(int levels, int maxNoOfPartitions, int maxNoOfLabels, String type) {
		String name = generateString(REGEX_PARTITION_NODE_NAME);
		String value = randomPartitionValue(type);
		
		PartitionNode partition = new PartitionNode(name, value);
		for(int i = 0; i < rand.nextInt(maxNoOfLabels + 1); i++){
			partition.addLabel(generateString(REGEX_PARTITION_LABEL));
		}
		
		if(levels > 0){
			for(int i = 0; i < rand.nextInt(maxNoOfPartitions); i++){
				partition.addPartition(generatePartition(levels - 1, maxNoOfPartitions, maxNoOfLabels, type));
			}
		}
		
		return partition;
	}



	private String randomType(){
		
		int typeIdx = rand.nextInt(SUPPORTED_TYPES.length + 1);
		if(typeIdx < SUPPORTED_TYPES.length){
			return SUPPORTED_TYPES[typeIdx];
		}
		
		return generateString(REGEX_CATEGORY_TYPE_NAME);
	}
	
	private String randomPartitionValue(String type){
		switch(type){
		case TYPE_NAME_BOOLEAN:
			return randomBooleanValue();
		case TYPE_NAME_BYTE:
			return randomByteValue();
		case TYPE_NAME_CHAR:
			return randomCharValue();
		case TYPE_NAME_DOUBLE:
			return randomDoubleValue();
		case TYPE_NAME_FLOAT:
			return randomFloatValue();
		case TYPE_NAME_INT:
			return randomIntValue();
		case TYPE_NAME_LONG:
			return randomLongValue();
		case TYPE_NAME_SHORT:
			return randomShortValue();
		case TYPE_NAME_STRING:
			return randomStringValue();
		default:
			return randomUserTypeValue();
		}
	}
	
	private String randomBooleanValue() {
		return String.valueOf(rand.nextBoolean());
	}

	private String randomByteValue() {
		String[] specialValues = {"MIN_VALUE", "MAX_VALUE"};
		
		if(rand.nextInt(5) == 0){
			return specialValues[rand.nextInt(specialValues.length)];
		}

		return String.valueOf((byte)rand.nextInt());
	}

	private String randomCharValue() {
		return String.valueOf((char)rand.nextInt());
	}

	private String randomDoubleValue() {
		String[] specialValues = {"MIN_VALUE", "MAX_VALUE", "POSITIVE_INFINITY", "NEGATIVE_INFINITY"};
		
		if(rand.nextInt(5) == 0){
			return specialValues[rand.nextInt(specialValues.length)];
		}

		return String.valueOf(rand.nextDouble());
	}



	private String randomFloatValue() {
		String[] specialValues = {"MIN_VALUE", "MAX_VALUE", "POSITIVE_INFINITY", "NEGATIVE_INFINITY"};
		
		if(rand.nextInt(5) == 0){
			return specialValues[rand.nextInt(specialValues.length)];
		}

		return String.valueOf(rand.nextLong());
	}

	private String randomIntValue() {
		String[] specialValues = {"MIN_VALUE", "MAX_VALUE"};
		
		if(rand.nextInt(5) == 0){
			return specialValues[rand.nextInt(specialValues.length)];
		}

		return String.valueOf(rand.nextInt());
	}

	private String randomLongValue() {
		String[] specialValues = {"MIN_VALUE", "MAX_VALUE"};
		
		if(rand.nextInt(5) == 0){
			return specialValues[rand.nextInt(specialValues.length)];
		}

		return String.valueOf(rand.nextLong());
	}

	private String randomShortValue() {
		String[] specialValues = {"MIN_VALUE", "MAX_VALUE"};
		
		if(rand.nextInt(5) == 0){
			return specialValues[rand.nextInt(specialValues.length)];
		}
		return String.valueOf((short)rand.nextInt());
	}

	private String randomStringValue() {
		return generateString(REGEX_STRING_TYPE_VALUE);
	}

	private String randomUserTypeValue() {
		return generateString(REGEX_USER_TYPE_VALUE);
	}

	private String generateString(String regex){
		Xeger generator = new Xeger(regex);
		return generator.generate();
	}
	
	//DEBUG
//	@Test
	public void testPartitionGeneration(){
		System.out.println("Childless partitions:");
		for(String type : new String[]{"String"}){
			PartitionNode p0 = generatePartition(0, 0, 0, type);
			System.out.println(type + " partition:" + p0);
		}
		
		System.out.println("Hierarchic partitions:");
		for(String type : SUPPORTED_TYPES){
			System.out.println("Type: " + type);
			PartitionNode p1 = generatePartition(MAX_PARTITION_LEVELS, MAX_PARTITIONS, MAX_PARTITION_LABELS, type);
			printPartition(p1, 0);
		}
	}
	
	private void printPartition(PartitionNode p, int indent){
		indent(indent);
		System.out.println(p + ", Labels: " + p.getLabels());
		for(PartitionNode child : p.getPartitions()){
			printPartition(child, indent + 4);
		}
	}
	
	private void indent(int indent){
		for(int i = 0; i < indent; i++){
			System.out.print(" ");
		}
	}
}
