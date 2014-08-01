package com.testify.ecfeed.modelif.java.common;

public class Messages {

	public static final String PARTITION_NAME_NOT_UNIQUE_PROBLEM = "Partition name must be unique within a category or parent partition";
	public static final String PARTITION_NAME_REGEX_PROBLEM = "Partition name must contain between 1 and 64 characters and do not contain only white space characters";
	public static final String PARTITION_VALUE_PROBLEM(String value){
		return "Value " + value + " is not valid for given parameter.\n\n" + 
				"Partition value must fit to type and range of the represented parameter.\n" +
				"Partitions of user defined type must follow Java enum defining rules.";
	}
	public static final String MODEL_NAME_REGEX_PROBLEM = "Model name must contain between 1 and 64 alphanumeric characters or spaces.\n The model name must not start with space.";
	public static final String CLASS_NAME_REGEX_PROBLEM = "The provided name must fulfill all rules for a qualified name of a class in Java";
	public static final String CLASS_NAME_CONTAINS_KEYWORD_PROBLEM = "The new class name contains Java keyword";
	public static final String CLASS_NAME_DUPLICATE_PROBLEM = "The model already contains a class with this name";
	public static final String CLASS_INDEX_NEGATIVE_PROBLEM = "The index of a class must be non-negative";
	public static final String CLASS_INDEX_TOO_HIGH_PROBLEM = "The index of a class must not be higher than number of classes in the model";
	public static final String CLASS_PARENT_DOES_NOT_EXIST_PROBLEM = "Missing current or new parent of moved class";

}
