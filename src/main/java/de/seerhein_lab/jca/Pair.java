package de.seerhein_lab.jca;

public class Pair<T, K> {
	private T value1;
	private K value2;

	public Pair(T value1, K value2) {
		this.value1 = value1;
		this.value2 = value2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value1 == null) ? 0 : value1.hashCode());
		result = prime * result + ((value2 == null) ? 0 : value2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Pair<?, ?>)) {
			return false;
		}
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (value1 == null) {
			if (other.value1 != null) {
				return false;
			}
		} else if (!value1.equals(other.value1)) {
			return false;
		}
		if (value2 == null) {
			if (other.value2 != null) {
				return false;
			}
		} else if (!value2.equals(other.value2)) {
			return false;
		}
		return true;
	}

}
