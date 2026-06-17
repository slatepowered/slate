package com.orbyfied.slate.util.collection;

import java.lang.reflect.Array;
import java.util.Collection;

public class ArrayUtil {

    @SuppressWarnings("unchecked")
    public static <T> T[] ensureCapacity(T[] arr, int size) {
        if (arr.length < size) {
            Object newArr = Array.newInstance(arr.getClass().getComponentType(), size);
            System.arraycopy(arr, 0, newArr, 0, arr.length);
            return (T[]) newArr;
        }

        return arr;
    }

    @SuppressWarnings("unchecked")
    public static <T> long[] ensureCapacity(long[] arr, int size) {
        if (arr.length < size) {
            long[] newArr = new long[arr.length];
            System.arraycopy(arr, 0, newArr, 0, arr.length);
            return newArr;
        }

        return arr;
    }

    @SuppressWarnings("unchecked")
    public static <T> int[] ensureCapacity(int[] arr, int size) {
        if (arr.length < size) {
            int[] newArr = new int[arr.length];
            System.arraycopy(arr, 0, newArr, 0, arr.length);
            return newArr;
        }

        return arr;
    }

    public static int firstZeroIndex(long[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 0) {
                return i;
            }
        }

        return -1;
    }

    public static int firstZeroOrNextIndex(long[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 0) {
                return i;
            }
        }

        return arr.length;
    }

    public static long[] replaceZeroOrAppend(long[] arr, long elem) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 0) {
                arr[i] = elem;
                return arr;
            }
        }

        return append(arr, elem);
    }

    public static <T> T[] appendOrSet(T[] arr, int index, T elem) {
        arr = ensureCapacity(arr, index + 1);
        arr[index] = elem;
        return arr;
    }

    public static long[] appendOrSet(long[] arr, int index, long elem) {
        arr = ensureCapacity(arr, index + 1);
        arr[index] = elem;
        return arr;
    }

    public static int[] appendOrSet(int[] arr, int index, int elem) {
        arr = ensureCapacity(arr, index + 1);
        arr[index] = elem;
        return arr;
    }

    @SuppressWarnings("unchecked")
    public static long[] append(long[] arr, long elem) {
        long[] newArr = new long[arr.length + 1];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = elem;
        return newArr;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] append(T[] arr, T elem) {
        Object newArr = Array.newInstance(arr.getClass().getComponentType(), arr.length + 1);
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        Array.set(newArr, arr.length, elem);
        return (T[]) newArr;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] append(T[] arr, T... elems) {
        Object newArr = Array.newInstance(arr.getClass().getComponentType(), arr.length + elems.length);
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        System.arraycopy(elems, 0, newArr, arr.length, elems.length);
        return (T[]) newArr;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] append(T[] arr, Collection<T> elems) {
        Object newArr = Array.newInstance(arr.getClass().getComponentType(), arr.length + elems.size());
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        System.arraycopy(elems.toArray(), 0, newArr, arr.length, elems.size());
        return (T[]) newArr;
    }

    public static int[] unbox(Integer[] array) {
        int[] ints = new int[array.length];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = array[i];
        }

        return ints;
    }

    public static <T> boolean contains(T[] arr, T elem) {
        for (T t : arr) {
            if (t == elem || elem.equals(t)) {
                return true;
            }
        }

        return false;
    }
}
