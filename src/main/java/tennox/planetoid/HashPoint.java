package tennox.planetoid;

public class HashPoint {

	int x, y;

	public HashPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public HashPoint() {
		this(0, 0);
	}

	public HashPoint set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HashPoint other = (HashPoint) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

}
