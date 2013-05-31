package com.testify.ecfeed.scanners;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

import com.testify.ecfeed.editors.ColorManager;
import com.testify.ecfeed.editors.XmlColorConstants;
import com.testify.ecfeed.editors.XmlWhitespaceDetector;

public class XmlScanner extends RuleBasedScanner {

	public XmlScanner(ColorManager manager) {
		IToken procInstr =
			new Token(new TextAttribute(manager.getColor(XmlColorConstants.PROC_INSTR)));

		IRule[] rules = new IRule[2];
		//Add rule for processing instructions
		rules[0] = new SingleLineRule("<?", "?>", procInstr);
		// Add generic whitespace rule.
		rules[1] = new WhitespaceRule(new XmlWhitespaceDetector());

		setRules(rules);
	}

}
