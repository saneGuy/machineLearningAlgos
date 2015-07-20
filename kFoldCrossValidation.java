package kFoldCrossValidation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

/*
 * each example object will have the features x1,x2 and classification y
 */
class example {
	int number; // example number
	int x1; // x1 is the first feature
	int x2; // x2 is the second feature
	char y; // y is the classification [+,-]
}

public class kFoldCrossValidation {
	static ArrayList<example> finalList;

	/*
	 * function to read file data to create variables corresponding to
	 * noOfExamples, noOfTimesItIsRun,permutationsOfExamples,k value
	 */
	static void readInput() throws FileNotFoundException {
		for (int kNN = 1; kNN <= 5; kNN++) {
			Scanner in = new Scanner(new File("file1Viswa.txt"));
			int k = in.nextInt(); // k no of folds
			int noOfExamples = in.nextInt(); // no of examples
			int noOfTimesToRun = in.nextInt(); // no of times to run k-fold
			String[] permutation;
			ArrayList<example> listOfExamples;
			ArrayList<example> listOfExamplesRearranged;
			double errorEstimates[] = new double[noOfTimesToRun];
			// take each permutation into permutation and run k-fold on it
			in.nextLine(); // this is to remove the extra empty line before the
							// permutations
			int noOfPerms = noOfTimesToRun;
			
			//boolean flag = true;
			while (in.hasNextLine() ) {
				permutation = in.nextLine().split(" ");
				listOfExamples = getExampleData();
				// Collections.shuffle(listOfExamples);
				listOfExamplesRearranged = reArrange(listOfExamples,
						permutation);
				//printList(listOfExamplesRearranged);
				errorEstimates[noOfTimesToRun - 1] = kFoldCrossValidationForKNearestNeighbours(
						listOfExamplesRearranged, k, kNN);
				noOfTimesToRun--;
				//flag = false;
			}
			System.out.println(noOfTimesToRun);
			// accurate estimate computation
			double accurateErrorEstimate = 0;
			double temp = 0;
			for (int i = 0; i < noOfPerms; i++) {
				temp = temp + errorEstimates[i];
			}
			accurateErrorEstimate = temp / noOfPerms;
			// variance and standard deviation computation
			temp = 0;
			for (int i = 0; i < noOfPerms; i++) {
				temp = temp
						+ Math.pow((accurateErrorEstimate - errorEstimates[i]),
								2);
			}
			double variance = 0;
			variance = temp / (noOfPerms - 1);
			double standardDeviation = 0;
			standardDeviation = Math.sqrt(variance);
			System.out
					.println("\n k = " + kNN + "  e = " + accurateErrorEstimate
							+ "  sigma =  " + standardDeviation);
			gridLabelling(kNN);
		}
	}

	static/*
		 * rearrange the given list as per the permutation and return a new
		 * arraylist
		 */
	ArrayList<example> reArrange(ArrayList<example> sourceList,
			String[] permutation) {
		ArrayList<example> rearrangedList = new ArrayList<>();
		for (int i = 0; i < permutation.length; i++) {
			rearrangedList
					.add(sourceList.get(Integer.parseInt(permutation[i])));
		}
		return rearrangedList;
	}

	/*
	 * print the example list
	 */
	static void printList(ArrayList<example> listOfExamples) {
		for (int i = 0; i < listOfExamples.size(); i++) {
			System.out.println(listOfExamples.get(i).number + " "
					+ listOfExamples.get(i).x1 + " " + listOfExamples.get(i).x2
					+ " " + listOfExamples.get(i).y + " ");
		}
		System.out.println();
	}

	/*
	 * permutation is the input. getExampleData() retrieves the examples from
	 * the data file
	 */
	static ArrayList<example> getExampleData() throws FileNotFoundException {
		ArrayList<example> exampleList = new ArrayList<>();
		int count = 0; // maintains the sequence for numbers
		Scanner in = new Scanner(new File("data.txt"));
		int noOfRows = in.nextInt();
		int noOfColumns = in.nextInt();
		in.nextLine();
		String[] row;
		// reading the data file to fetch the classifications
		for (int i = 0; i < noOfRows; i++) {
			row = in.nextLine().split(" ");
			for (int j = 0; j < row.length; j++) {
				if (row[j].equals("+")) {
					example newExample = new example();
					newExample.x2 = i;
					newExample.x1 = j;
					newExample.y = '+';
					newExample.number = count;
					count++;
					exampleList.add(newExample);
				} else if (row[j].equals("-")) {
					example newExample = new example();
					newExample.x2 = i;
					newExample.x1 = j;
					newExample.y = '-';
					newExample.number = count;
					count++;
					exampleList.add(newExample);
				}
			}
		}
		return exampleList;
	}

	static/*
		 * function to do k fold cross validation and return the error estimate
		 */
	double kFoldCrossValidationForKNearestNeighbours(
			ArrayList<example> listOfExamples, int k, int kNN) {
		boolean label = false;
		int noOfExamples = listOfExamples.size();
		int sizeOfFold = noOfExamples / k;
		double totalNoOfErrors = 0;
		double errorsInIthIteration = 0;
		boolean lastFold = false;
		for (int i = 0; i + sizeOfFold < noOfExamples; i = i + sizeOfFold) {
			// set a flag = true saying that is the last fold so that all the
			// examples till the end of the list are taken for testing
			// System.out.println("fold first ex number: " + i);
			// except i train the classifier with remaining folds
			if (k == 1) {
				lastFold = true;
			}
			errorsInIthIteration = kNearestNeighbours(listOfExamples, i,
					sizeOfFold, kNN, lastFold, label);
			totalNoOfErrors = totalNoOfErrors + errorsInIthIteration;
			// System.out.println(errorsInIthIteration);
			k--;
		}
		return totalNoOfErrors / noOfExamples;
	}

	/*
	 * function to do k-nearest neighbors. Use foldNo to test the examples in
	 * the fold against the remaining folds and return the no of misclassified
	 * examples in each fold. We need to do it for k = 1,2,3,4,5 so lets write
	 * some generic code for k-value
	 */
	static int kNearestNeighbours(ArrayList<example> listOfExamples,
			int exNoInFold, int sizeOfFold, int k, boolean lastFold,
			boolean label) {
		// this will hold the k nearest neighbors distances to the example in
		// consideration
		double[] distances = new double[listOfExamples.size()]; // this will
																// hold the
																// distances of
																// the examples
																// from the
																// example that
																// is tested
		// example no will be the index and the distance will be the value
		int noOfMisClassifiedExamples = 0;
		for (int i = exNoInFold; i < exNoInFold + sizeOfFold || lastFold; i++) {
			// if we are labelling the grid and the y is not '.'
			// then we don't compute the distances to it
			if (label && listOfExamples.get(i).y != '.') {
				continue;
			}
			// if the fold is the last one then the all the remaining examples
			// are included
			// in that fold
			if (lastFold) {
				if (i >= listOfExamples.size()) {
					break;
				}
			}
			// for each example in the fold we will compute distances to all the
			// non fold examples
			for (int j = 0; j < listOfExamples.size(); j++) {
				if (lastFold) {
					if (j >= exNoInFold && j < listOfExamples.size()) {
						// distances in the folds are initialized to MAX_VALUE
						distances[j] = Double.MAX_VALUE;
						continue;
					}
				} else {
					if (j >= exNoInFold && j < exNoInFold + sizeOfFold) {
						// distances in the folds are initialized to MAX_VALUE
						distances[j] = Double.MAX_VALUE;
						continue;
					}
				}
				if (listOfExamples.get(j).y != '.') {
					distances[j] = distance(listOfExamples.get(i),
							listOfExamples.get(j));
				} else {
					distances[j] = Double.MAX_VALUE;
				}
			}
			// now we have all the examples with their distances to the example
			// in fold lets take the k smallest distances and find the majority
			// y in
			// those examples and check if it is same as the y values of the
			// example
			// if it is not increment the no of misclassified examples in this
			// fold
			double minDistance;
			int exampleNumber = -1;
			int noOfPosExamples = 0;
			int noOfNegExamples = 0;
			HashMap<example, Double> sameDistanceExamples = new HashMap<>();
			double globalMin = Double.MAX_VALUE;
			// here we are computing k minimums in the distances array
			for (int p = 0; p < k; p++) {
				minDistance = Double.MAX_VALUE;
				for (int m = 0; m < distances.length; m++) {
					//System.out.print(distances[m] + " ");
					if (minDistance > distances[m]) {
						minDistance = distances[m];
						exampleNumber = m;
					} else if (minDistance != Double.MAX_VALUE
							&& minDistance == distances[m]) {
						sameDistanceExamples.put(listOfExamples.get(m),
								distances[m]);
					}
				}
				globalMin = distances[exampleNumber];
				//System.out.println();
				distances[exampleNumber] = Double.MAX_VALUE;
				if (listOfExamples.get(exampleNumber).y == '+') {
					noOfPosExamples++;
				} else if (listOfExamples.get(exampleNumber).y == '-') {
					noOfNegExamples++;
				}
				Set<example> S = sameDistanceExamples.keySet();
				if (!sameDistanceExamples.isEmpty()) {
					for (example e : S) {
						if (sameDistanceExamples.get(e) == globalMin
								&& e.y == '+') {
							noOfPosExamples++;
						} else if (sameDistanceExamples.get(e) == globalMin
								&& e.y == '-') {
							noOfNegExamples++;
						}
					}
				}
				sameDistanceExamples.clear();
			} // System.out.println();
				// checking the majority y values for the examples
				// verifying if the fold example has the same classification
				// if it is different from the test examples classification
				// we are wrongly classifying the example and increment
				// the noOfMisClassifiedExamples
			if (noOfPosExamples > noOfNegExamples) {
				if (listOfExamples.get(i).y == '.') {
					finalList.get(i).y = '+';
				} else if (!label && listOfExamples.get(i).y != '+') {
					noOfMisClassifiedExamples++;
				}
			} else {
				if (listOfExamples.get(i).y == '.') {
					finalList.get(i).y = '-';
				} else if (!label && listOfExamples.get(i).y != '-') {
					noOfMisClassifiedExamples++;
				}
			}
		}

		/*
		 * if (label) { printMatrix(listOfExamples, 4, 5); }
		 */return noOfMisClassifiedExamples;
	}

	/*
	 * distance will compute the euclidean distance between two examples
	 */
	static double distance(example e1, example e2) {
		return Math.pow((e1.x1 - e2.x1), 2) + Math.pow((e1.x2 - e2.x2), 2);
	}

	/*
	 * label the whole grid based on k nearest neighbor algorithm for each given
	 * k value
	 */
	static void gridLabelling(int k) throws FileNotFoundException {
		// for (int k = 1; k <= 5; k++) {
		ArrayList<example> exampleList = new ArrayList<>();
		finalList = new ArrayList<>();
		int count = 0; // maintains the sequence for numbers
		Scanner in = new Scanner(new File("data.txt"));
		int noOfRows = in.nextInt();
		int noOfColumns = in.nextInt();
		in.nextLine();
		String[] row;

		// reading the data file to fetch the classifications
		for (int i = 0; i < noOfRows; i++) {
			row = in.nextLine().split(" ");
			for (int j = 0; j < row.length; j++) {
				if (row[j].equals("+")) {
					example newExample = new example();
					newExample.x2 = i;
					newExample.x1 = j;
					newExample.y = '+';
					newExample.number = count;
					example finalExample = new example();
					finalExample.x2 = i;
					finalExample.x1 = j;
					finalExample.y = '+';
					finalExample.number = count;
					count++;
					exampleList.add(newExample);
					finalList.add(finalExample);
				} else if (row[j].equals("-")) {
					example newExample = new example();
					newExample.x2 = i;
					newExample.x1 = j;
					newExample.y = '-';
					newExample.number = count;
					exampleList.add(newExample);
					example finalExample = new example();
					finalExample.x2 = i;
					finalExample.x1 = j;
					finalExample.y = '-';
					finalExample.number = count;
					finalList.add(finalExample);
					count++;
				} else {
					example newExample = new example();
					newExample.x2 = i;
					newExample.x1 = j;
					newExample.y = '.';
					newExample.number = count;
					exampleList.add(newExample);
					example finalExample = new example();
					finalExample.x2 = i;
					finalExample.x1 = j;
					finalExample.y = '.';
					finalExample.number = count;
					finalList.add(finalExample);
					count++;
				}
			}
		}
		for (int i = 0; i < exampleList.size(); i++) {
			kNearestNeighbours(exampleList, i, 1, k, false, true);
		}
		printMatrix(finalList, noOfRows, noOfColumns);
		// printList(finalList);
	}

	/*
	 * prints the grid using the list of examples
	 */
	static void printMatrix(ArrayList<example> exampleList, int noOfRows,
			int noOfColumns) {
		System.out.println();
		for (int i = 0; i < exampleList.size(); i = i + noOfColumns) {
			for (int j = i; j < i + noOfColumns; j++) {
				System.out.print(exampleList.get(j).y + " ");
			}
			System.out.println();
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		readInput();
	}

}
