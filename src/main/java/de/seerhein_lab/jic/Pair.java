package de.seerhein_lab.jic;

public class Pair<T, K> {
	private T value1;
	private K value2;

	public Pair(T value1, K value2) {
		if (value1 == null || value2 == null)
			throw new NullPointerException("Pair components must not be null");

		this.value1 = value1;
		this.value2 = value2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value1.hashCode();
		result = prime * result + value2.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Pair<?, ?>)) {
			return false;
		}
		Pair<?, ?> other = (Pair<?, ?>) obj;

		return value1.equals(other.value1) && value2.equals(other.value2);
	}

}
