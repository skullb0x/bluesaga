package utils;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Spiral {
	private enum Direction {
		E(1, 0) {Direction next() {return N;}},
		N(0, 1) {Direction next() {return W;}},
		W(-1, 0) {Direction next() {return S;}},
		S(0, -1) {Direction next() {return E;}},;

		private int	dx;
		private int	dy;

		Point advance(Point point) {
			return new Point(point.x + dx, point.y + dy);
		}

		abstract Direction next();

		Direction(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
		}
	};
	private final static Point ORIGIN = new Point(0, 0);
	private final int	width;
	private final int	height;
	private Point		point;
	private Direction	direction	= Direction.E;
	private List<Point>	list = new ArrayList<Point>();

	public Spiral(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public List<Point> spiral() {
		point = ORIGIN;
		int steps = 1;
		while (list.size() < width * height) {
			advance(steps);
			advance(steps);
			steps++;
		}
		return list;
	}

	private void advance(int n) {
		for (int i = 0; i < n; ++i) {
			if (inBounds(point))
				list.add(point);
			point = direction.advance(point);
		}
		direction = direction.next();
	}

	private boolean inBounds(Point p) {
		return between(-width / 2, width / 2, p.x) && between(-height / 2, height / 2, p.y);
	}

	private static boolean between(int low, int high, int n) {
		return low <= n && n <= high;
	}
}