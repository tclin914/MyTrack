package com.demo.android.mytrack;

public class QuickSort {
	public void sort(double [] array){
		quickSort(array, 0, array.length-1);
	}
	public void quickSort(double [] arr, int left, int right){
		if(left>=right){
			return;
		}else{
			double key=arr[right];
			int min=partion(arr, left, right, key);
			quickSort(arr, left, min-1);
			quickSort(arr, min+1, right);
		}
	}
	public int partion(double[] arr, int left, int right, double key){
		int leftScan=left-1;
		int rightScan=right+1;
		while(true){
			while (leftScan < right && arr[++leftScan] < key);
			while (rightScan > left && arr[--rightScan] > key);
			if (leftScan >= rightScan)
				break;
			else{
				swap(arr, leftScan, rightScan); 
			}
		}
		return leftScan;
	}
	public void swap(double[] arr, int i, int j){
		double temp=arr[i];
		arr[i]=arr[j];
		arr[j]=temp;
	}

}
