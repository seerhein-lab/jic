package de.htwg_konstanz.in.jca;

import java.util.Stack;

import org.apache.bcel.classfile.LocalVariable;

public class LocalVars {
	private Entry[] entries;
	private int[] indexes;

	LocalVars(LocalVariable[] localVarTable) {
		if (localVarTable == null) {
			throw new IllegalArgumentException("localVarTable must not be null");
		}
		entries = new Entry[localVarTable.length];
		indexes = new int[localVarTable.length];

		for (int i = 0; i < entries.length; i++)
			entries[i] = Entry.getInstance(localVarTable[i].getSignature());

		for (int i = 0; i < indexes.length; i++)
			indexes[i] = localVarTable[i].getIndex();
	}

	public void initWithArgs(Stack<Entry> callerStack, int numArgs) {
		if (entries.length > 0)
			for (int i = numArgs - 1; i >= 0; i--)
				entries[i] = callerStack.pop();
	}

	private int index2i(int index) {
		for (int i = 0; i < indexes.length; i++) {
			if (indexes[i] == index)
				return i;
		}
		throw new AssertionError("invalid index");
	}

	public Entry getForIndex(int index) {
		return entries[index2i(index)];
	}

	public void setForIndex(int index, Entry entry) {
		entries[index2i(index)] = entry;
	}

	public int getIndex(int i) {
		return indexes[i];
	}

	public Entry getEntry(int i) {
		return entries[i];
	}

	public int getEntriesLength() {
		return entries.length;
	}

	public int getIndexesLength() {
		return indexes.length;
	}
}
