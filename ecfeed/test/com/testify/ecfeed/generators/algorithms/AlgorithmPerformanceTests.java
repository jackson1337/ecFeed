package com.testify.ecfeed.generators.algorithms;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.testify.ecfeed.generators.api.GeneratorException;
import com.testify.ecfeed.generators.api.IConstraint;
import com.testify.ecfeed.generators.utils.GeneratorTestUtils;

public class AlgorithmPerformanceTests {
	private final Collection<IConstraint<String>> EMPTY_CONSTRAINTS = new ArrayList<IConstraint<String>>();

//	@Test
	public void totalDistanceTest(){
		try{
		for(int n = 2; n < 4; n++){
		System.out.println("N: " + n);
					
		List<List<String>> input = GeneratorTestUtils.prepareInput(5, 6);
		long totalTuples = calculateTotalTuples(input, n);
		
		AdaptiveRandomAlgorithm<String> adRandom = new AdaptiveRandomAlgorithm<String>(-1, 1000, 1000, false);
		AdaptiveRandomAlgorithm<String> random = new AdaptiveRandomAlgorithm<String>(0, 1, 1000, false);
		OptimalNWiseAlgorithm<String> nwise = new OptimalNWiseAlgorithm<String>(n);
		CartesianProductAlgorithm<String> cart = new CartesianProductAlgorithm<String>();
		
		adRandom.initialize(input, EMPTY_CONSTRAINTS);
		random.initialize(input, EMPTY_CONSTRAINTS);
		nwise.initialize(input, EMPTY_CONSTRAINTS);
		cart.initialize(input, EMPTY_CONSTRAINTS);
		
		List<List<String>> adRandomSuite = new ArrayList<List<String>>();
		List<List<String>> randomSuite = new ArrayList<List<String>>();
		List<List<String>> nwiseSuite = new ArrayList<List<String>>();
		List<List<String>> cartSuite = new ArrayList<List<String>>();
		
		List<String> next = null;
		
		while((next = nwise.getNext()) != null){
			nwiseSuite.add(next);
		}
		while((next = random.getNext()) != null && randomSuite.size() < nwiseSuite.size()){
			randomSuite.add(next);
		}
		while((next = adRandom.getNext()) != null && adRandomSuite.size() < nwiseSuite.size()){
			adRandomSuite.add(next);
		}
		while((next = cart.getNext()) != null && cartSuite.size() < nwiseSuite.size()){
			cartSuite.add(next);
		}
		
		System.out.println(n + "-wise suite size: " + adRandomSuite.size());
		System.out.println("total tuples: " + totalTuples);
		
		double avgNWiseDistance = calculateAvgDistance(nwiseSuite);
		double avgAdRandomDistance = calculateAvgDistance(adRandomSuite);
		double avgRandomDistance = calculateAvgDistance(randomSuite);
		double avgCartDistance = calculateAvgDistance(cartSuite);

		int nwiseTuplesCovered = calculateCoveredTuples(nwiseSuite, input, n);
		int adRandomTuplesCovered = calculateCoveredTuples(adRandomSuite, input, n);
		int randomTuplesCovered = calculateCoveredTuples(randomSuite, input, n);
		int cartTuplesCovered = calculateCoveredTuples(cartSuite, input, n);
		
		System.out.println("nwise covered " + n + "-tuples: " + nwiseTuplesCovered);
		System.out.println("random covered " + n + "-tuples: " + randomTuplesCovered);
		System.out.println("adaptive covered " + n + "-tuples: " + adRandomTuplesCovered);
		System.out.println("cart covered " + n + "-tuples: " + cartTuplesCovered);
		
		System.out.println("Avg nwise distance: " + avgNWiseDistance);
		System.out.println("Avg random distance: " + avgRandomDistance);
		System.out.println("Avg adaptive random distance: " + avgAdRandomDistance);
		System.out.println("Avg cart distance: " + avgCartDistance);
		}
		}
		catch(GeneratorException e){
			fail("Unexpected GeneratorException: " + e.getMessage());
		}
	}

//	@Test
	public void randomVsAdaptiveTest(){
		List<List<String>> input = GeneratorTestUtils.prepareInput(30, 2);
		for(int length : new int[]{5, 10, 50, 100}){
			System.out.println("length: " + length);
			try{
				AdaptiveRandomAlgorithm<String> adRandom = new AdaptiveRandomAlgorithm<String>(100, 100, length, false);
				AdaptiveRandomAlgorithm<String> random = new AdaptiveRandomAlgorithm<String>(0, 1, length, false);

				adRandom.initialize(input, EMPTY_CONSTRAINTS);
				random.initialize(input, EMPTY_CONSTRAINTS);

				List<List<String>> adRandomSuite = new ArrayList<List<String>>();
				List<List<String>> randomSuite = new ArrayList<List<String>>();
				List<String> next = null;

				while((next = random.getNext()) != null){
					randomSuite.add(next);
				}
				while((next = adRandom.getNext()) != null){
					adRandomSuite.add(next);
				}
				double avgAdRandomDistance = calculateAvgDistance(adRandomSuite);
				double avgRandomDistance = calculateAvgDistance(randomSuite);

				System.out.println("Avg random distance: " + avgRandomDistance);
				System.out.println("Avg adaptive random distance: " + avgAdRandomDistance);
			}
			catch(GeneratorException e){
				fail("Unexpected exception: " + e.getMessage());
			}
		}
	}
	
	private long calculateTotalTuples(List<List<String>> input, int n){
		long totalWork = 0;
		Tuples<List<String>> tuples = new Tuples<List<String>>(input, n);
		while(tuples.hasNext()){
			long combinations = 1;
			List<List<String>> tuple = tuples.next();
			for(List<String> category : tuple){
				combinations *= category.size();
			}
			totalWork += combinations;
		}
		return totalWork;
	}

	private int calculateCoveredTuples(List<List<String>> suite,
			List<List<String>> input, int n) {
		Set<List<String>> tuplesCovered = new HashSet<List<String>>();
		for(List<String> testCase : suite){
			Tuples<String> tuples = new Tuples<String>(testCase, n);
			tuplesCovered.addAll(tuples.getAll());
		}
		return tuplesCovered.size();
	}

	private double calculateAvgDistance(List<List<String>> suite) {
		int totalDistance = 0;
		int distCount = 0;
		AdaptiveRandomAlgorithm<String> random = new AdaptiveRandomAlgorithm<>(0, 1, 0, false);
		for(List<String> testCase : suite){
			for(List<String> other : suite){
				if(testCase != other){
					totalDistance += random.distance(testCase, other);
					distCount++;
				}
			}
		}
		return (double)totalDistance / (double)distCount;
	}

}
