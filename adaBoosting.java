/**
 * 
 */
package adaBoosting;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Viswanadha Pratap Kondoju
 *
 */
class classifier {
	hypothesis h1;
	double alpha;
	double fractionalError;
	double boundOnEt;
}

class example {
	double x;
	int y;
	double prob;
	int num;
	int h; // classifier classification
}

class hypothesis {
	double threshold;
	boolean left;
	double weightedTrainingError;
}

public class adaBoosting {

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	static int noOfIter;

	static/*
		 * reading the input from the input file
		 */
	ArrayList<example> readInput() throws FileNotFoundException {
		Scanner in = new Scanner(new File("input.txt.txt"));
		// reading number of iterations
		noOfIter = in.nextInt();
		// reading no of examples
		int noOfEx = in.nextInt();
		// epsilon
		double epsilon = in.nextDouble();
		// reading the x values of the example objects
		ArrayList<example> listOfExamples = new ArrayList<>();
		for (int i = 0; i < noOfEx; i++) {
			example ex = new example();
			ex.x = in.nextDouble();
			ex.num = i;
			listOfExamples.add(ex);
		}
		// System.out.println(listOfExamples.size());
		// reading the y values of the example objects
		for (int i = 0; i < noOfEx; i++) {
			// System.out.println(i);
			listOfExamples.get(i).y = in.nextInt();
		}
		// reading the probabilities for the examples
		for (int i = 0; i < noOfEx; i++) {
			listOfExamples.get(i).prob = in.nextDouble();
		}

		return listOfExamples;

	}

	/*
	 * print the list
	 */
	static void printList(ArrayList<example> listOfExamples) {
		for (int i = 0; i < listOfExamples.size(); i++) {
			System.out.println(listOfExamples.get(i).num + " "
					+ listOfExamples.get(i).x + " " + listOfExamples.get(i).y
					+ " " + listOfExamples.get(i).h + " "
					+ listOfExamples.get(i).prob);
		}
	}

	static/*
		 * using all the examples and the probabilities find the hypothesis and
		 * return the value or threshold value
		 */
	hypothesis getHypothesis(ArrayList<example> listOfExamples) {
		// we need to split the list b/w each pair of examples and see how
		// many are mis classified and sum their probabilities
		// which ever hypothesis has the minimum probability
		// we will chose that as our classifier
		int noOfMisClassifiedEx = 0;
		double sumOfProbOfMisClassEx;
		double minOfAllThresholds = Double.MAX_VALUE;
		int minOfAllEx = 0;
		boolean globalLeft = false;
		for (int i = 1; i <= listOfExamples.size() - 1; i++) {
			// splitting b/w i-1 and i and counting the probabilities
			// for misclassified examples counting the probabilities
			// for examples below the threshold we need to check two
			// cases here one all the ex below the threshold are positive
			// and the ones above the threshold are negative
			// and the other case is the reverse . Among these two
			// choose the one with minimum no of errors
			boolean leftPos = true;
			boolean right;
			boolean left = right = false;
			int noOfCases = 2;
			double minProbSum = Double.MAX_VALUE;
			while (noOfCases != 0) {
				sumOfProbOfMisClassEx = 0;
				if (noOfCases != 2) {
					leftPos = false;
				}
				for (int j = 0; j < i; j++) {
					if (leftPos) {
						// if lefts are positive and the classification of the
						// ex
						// in the left fold is not 1 then it is misclassified
						if (listOfExamples.get(j).y != 1) {
							sumOfProbOfMisClassEx = sumOfProbOfMisClassEx
									+ listOfExamples.get(j).prob;

						}
					} else if (!leftPos) {
						// if rights are positive and the classification of the
						// ex
						// in the left fold is not -1 then it is misclassified
						if (listOfExamples.get(j).y != -1) {
							sumOfProbOfMisClassEx = sumOfProbOfMisClassEx
									+ listOfExamples.get(j).prob;

						}

					}

				}
				// counting probabilities for examples above the threshold
				for (int j = listOfExamples.size() - 1; j >= i; j--) {
					if (!leftPos) {
						// if rights are positive and the classification of the
						// ex
						// in the right fold is not 1 then it is misclassified
						if (listOfExamples.get(j).y != 1) {
							sumOfProbOfMisClassEx = sumOfProbOfMisClassEx
									+ listOfExamples.get(j).prob;

						}
					} else if (leftPos) {
						// if lefts are positive and the classification of the
						// ex
						// in the right fold is not -1 then it is misclassified
						if (listOfExamples.get(j).y != -1) {
							sumOfProbOfMisClassEx = sumOfProbOfMisClassEx
									+ listOfExamples.get(j).prob;

						}

					}

				}
				if (minProbSum > sumOfProbOfMisClassEx) {
					minProbSum = sumOfProbOfMisClassEx;
					if (leftPos) {
						left = true;
					} else {
						left = false;
					}

				}
				noOfCases--;
			}

			//System.out.println(minProbSum);
			if (minOfAllThresholds > minProbSum) {

				minOfAllThresholds = minProbSum;
				minOfAllEx = i;
				if (left) {
					globalLeft = true;
				} else {
					globalLeft = false;
				}
			}

		}
		hypothesis hCurrent = new hypothesis();
		hCurrent.threshold = (listOfExamples.get(minOfAllEx).x + listOfExamples
				.get(minOfAllEx - 1).x) / 2;
		hCurrent.left = globalLeft;
		hCurrent.weightedTrainingError = minOfAllThresholds;
		return hCurrent;
	}
	
	static/*
		 * function to print the boosted classifier
		 */
	void printBoostedClassifier(ArrayList<classifier> boostedClassifier) {

		for (int i = 0; i < boostedClassifier.size(); i++) {
			if (boostedClassifier.get(i).h1.left) {
				System.out.print(boostedClassifier.get(i).alpha + " * ( x < "
						+ boostedClassifier.get(i).h1.threshold + " )");
			} else {
				System.out.print(boostedClassifier.get(i).alpha + " * ( x > "
						+ boostedClassifier.get(i).h1.threshold + " )");

			}
			if (boostedClassifier.size() > 1
					&& i < boostedClassifier.size() - 1)
				System.out.print(" + ");

		}
		System.out.println();
	}

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		ArrayList<example> listOfExamples;
		// reading input from the input file input.txt.txt
		listOfExamples = readInput();
		// print the list that is just read
		// printList(listOfExamples);
		// get the hypothesis from the examples
		hypothesis h1;
		// debug and figure out the sign properly
		double[] alphas = new double[noOfIter];
		double[] Zt = new double[noOfIter];
		// boosted classifier
		// this will hold the hypothesis and the corresponding goodness weights
		// for
		// each iteration
		ArrayList<classifier> boostedClassifier = new ArrayList<>();
		// wrongCount is used to count the wrongly classified examples by the
		// classifier
		int wrongCount;
		double Et;
		double bound;
		for (int i = 0; i < noOfIter; i++) {
			System.out.println("Running iteration : " + i);
			printList(listOfExamples);
			h1 = getHypothesis(listOfExamples);
			updateClassicationForHypotheis(listOfExamples, h1);

			/*
			 * if(h1.left) System.out.println("example is positive if x is < "
			 * +h1.threshold + " with error " + h1.weightedTrainingError); else
			 * System.out.println("example is positive if x is > " +h1.threshold
			 * + " with error " + h1.weightedTrainingError);
			 */
			/*
			 * if the classifier is not week we can break and run the algorithm
			 * again
			 */
			if (h1.weightedTrainingError == 0) {
				System.out.println("classifier is not week Run the algo again");
				break;
			}

			// computing the goodness weight of hypothesis
			alphas[i] = (0.5) * (Math.log((1 - h1.weightedTrainingError)
					/ h1.weightedTrainingError));

			// computing the Zt value

			Zt[i] = 2 * Math.sqrt(h1.weightedTrainingError
					* (1 - h1.weightedTrainingError));

			// computing the updated probabilities
			wrongCount = 0;
			for (int j = 0; j < listOfExamples.size(); j++) {
				// if(h1.left)
				listOfExamples.get(j).prob = (listOfExamples.get(j).prob * Math
						.pow(Math.E,
								-(alphas[i] * listOfExamples.get(j).y * listOfExamples
										.get(j).h)))
						/ Zt[i];
				if (listOfExamples.get(j).y != listOfExamples.get(j).h) {
					wrongCount++;
				}
			}
			// adding h1,alpha from each iteration to the boostedClassifier
			classifier localClassifier = new classifier();
			localClassifier.h1 = h1;
			localClassifier.alpha = alphas[i];
			// computing fractional error of the boosted classifier Et
			localClassifier.fractionalError = wrongCount
					/ listOfExamples.size();
			bound = 1;
			// computing the bound on Et
			for (int k = 0; k < i; k++) {
				bound = bound * Zt[k];
			}
			localClassifier.boundOnEt = bound;
			boostedClassifier.add(localClassifier);
			printBoostedClassifier(boostedClassifier);
		}
	}

	/*
	 * updates the hypothesized classification in the given list of examples
	 * using the hypothesis obtained
	 */
	private static void updateClassicationForHypotheis(
			ArrayList<example> listOfExamples, hypothesis h1) {
		// TODO Auto-generated method stub
		for (int i = 0; i < listOfExamples.size(); i++) {
			if (listOfExamples.get(i).x < h1.threshold) {
				if (h1.left) {
					listOfExamples.get(i).h = 1;
				} else {
					listOfExamples.get(i).h = -1;
				}
			} else {
				if (h1.left) {
					listOfExamples.get(i).h = -1;
				} else {
					listOfExamples.get(i).h = 1;
				}
			}
		}

	}

}
