package de.htwg_konstanz.in.jca;

import java.util.Stack;

import org.apache.bcel.classfile.LocalVariable;

/**
 * Keeps the local variables of constructors and/or methods with it's entries
 * and indexes.
 */
public class LocalVars {
	private Entry[] entries;
	private int[] indexes;

	/**
	 * Constructor. Initializes the entries- and indexes-arrays and fills it
	 * with the values from the localVarTable.
	 * 
	 * @param localVarTable
	 *            The array with the LocalVariables of the constructor/method.
	 */
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

	/**
	 * Copy-constructor. Deep-copy of the Entry- and Indexes-array of an
	 * existing localVars object.
	 * 
	 * @param localVars
	 *            The localVars object which values are to be copied.
	 */
	public LocalVars(LocalVars localVars) {
		this.entries = new Entry[localVars.getEntriesLength()];
		this.indexes = new int[localVars.getIndexesLength()];
		for (int i = 0; i < entries.length; i++) {
			entries[i] = localVars.getEntry(i);
			indexes[i] = localVars.getIndex(i);
		}
	}

	/**
	 * Writes numArgs arguments from callerStack to entries.
	 * 
	 * @param callerStack
	 *            The stack of the caller constructor/method.
	 * @param numArgs
	 *            The number of arguments to be copied from the callerStack.
	 */
	public void initWithArgs(Stack<Entry> callerStack, int numArgs) {
		if (entries.length > 0)
			for (int i = numArgs - 1; i >= 0; i--)
				entries[i] = callerStack.pop();
		else
			for (int i = numArgs - 1; i >= 0; i--)
				callerStack.pop();
	}

	private int index2i(int index) {
		for (int i = 0; i < indexes.length; i++) {
			if (indexes[i] == index)
				return i;
		}
		throw new AssertionError("invalid index");
	}

	/**
	 * Returns the entry with indexes index.
	 * 
	 * @param index
	 *            The index of the needed entry on the local variables.
	 * @return The entry with indexes index.
	 */
	public Entry getForIndex(int index) {
		return entries[index2i(index)];
	}

	/**
	 * Sets the entry with indexes index.
	 * 
	 * @param index
	 *            The index of the entry on the local variables.
	 * @param entry
	 *            The entry to set.
	 */
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

	/**
	 * Returns all values of entries and indexes as a String. The format is
	 * [entry_0 (indexes_0), entry_1 (indexes_1), ... entry_n-1 (indexes_n-1)].
	 */
	public String toString() {
		String string = "";
		for (int i = 0; i < entries.length; i++) {
			if (i == 0) {
				string += "[";
			}
			string += (entries[i] + "(" + indexes[i] + ")");
			string += (i == entries.length - 1) ? "]" : ", ";
		}
		return string;
	}
}
