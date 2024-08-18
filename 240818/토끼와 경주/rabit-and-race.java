import java.util.*;
import java.io.*;

public class Main {

	public static class Rabbit implements Comparable<Rabbit> {
		int y, x;
		int pid;
		int d;
		int jmpCount;
		long currentScore;
		long rabbitScore;

		public Rabbit(int pid, int d) {
			this.y = 0;
			this.x = 0;
			this.pid = pid;
			this.d = d;

			this.jmpCount = 0;
			this.currentScore = 0;
			this.rabbitScore = 0L;
		}

		@Override
		public int compareTo(Rabbit o) {
			int compare = Integer.compare(this.jmpCount, o.jmpCount);

			if (compare == 0) {
				compare = Integer.compare(this.y + this.x, o.y + o.x);
				if (compare == 0) {
					compare = Integer.compare(this.y, o.y);
					if (compare == 0) {
						compare = Integer.compare(this.x, o.x);
						if (compare == 0) {
							compare = Integer.compare(this.pid, o.pid);
						}
					}
				}
			}

			return compare;
		}
	}

	public static class Location implements Comparable<Location> {
		int y;
		int x;
		int dir;

		public Location(int y, int x, int dir) {
			this.y = y;
			this.x = x;
			this.dir = dir;
		}

		@Override
		public int compareTo(Location o) {
			int compare = Integer.compare(o.y + o.x, this.y + this.x);

			if (compare == 0) {
				compare = Integer.compare(o.y, this.y);
				if (compare == 0) {
					compare = Integer.compare(o.x, this.x);
				}
			}

			return compare;
		}

		@Override
		public String toString() {
			return "(" + y + ", " + x + ", " + dir + ")";
		}
	}

	private static int N, M;
	private static PriorityQueue<Rabbit> rabbitPriorityQueue;
	private static long totalScore = 0L;
	private static final int[] dy = {-1, 0, 1, 0};
	private static final int[] dx = {0, -1, 0, 1};
	private static Map<Integer, Rabbit> rabbitMap;

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st;

		int Q = Integer.parseInt(br.readLine());

		for (int i = 0; i < Q; i++) {
			st = new StringTokenizer(br.readLine());

			int cmd = Integer.parseInt(st.nextToken());
			if (cmd == 100) {
				int n = Integer.parseInt(st.nextToken());
				int m = Integer.parseInt(st.nextToken());
				int p = Integer.parseInt(st.nextToken());

				List<Integer> inputList = new ArrayList<>(4000);

				while(st.hasMoreTokens()) {
					inputList.add(Integer.parseInt(st.nextToken()));
				}

				init(n, m, p, inputList);
			} else if (cmd == 200) {
				int K = Integer.parseInt(st.nextToken());
				int S = Integer.parseInt(st.nextToken());

				racing(K, S);
			} else if (cmd == 300) {
				int pid = Integer.parseInt(st.nextToken());
				int L = Integer.parseInt(st.nextToken());

				changeDistance(pid, L);
			} else if (cmd == 400) {
				System.out.println(getBestRabbit());
			}
		}

	}

	// 모든 토끼들의 점수들을 누적합한다.
	// 이후 각 토끼들이 받은 점수들을 각각 뺀다. 이후 이걸 Priority Queue 에 넣는다.
	// 초기 위치 0, 0 으로 하는게 정신건강에 좋을것 같다.
	public static void init(int n, int m, int p, List<Integer> inputList) {
		N = n; M = m;

		rabbitMap = new HashMap<>();
		rabbitPriorityQueue = new PriorityQueue<>();

		for (int i = 0, j = 0; i < p; i++, j += 2) {
			int pid = inputList.get(j);
			int d = inputList.get(j + 1);

			Rabbit rabbit = new Rabbit(pid, d);
			rabbitMap.put(pid, rabbit);
			rabbitPriorityQueue.add(rabbit);
		}
	}

	public static void racing(int K, int S) {
		List<Rabbit> participatedRabbit = new ArrayList<>();

		for (int i = 0; i < K; i++) {
			Rabbit tgtRabbit = rabbitPriorityQueue.poll();

			List<Location> locations = new ArrayList<>();
			for (int j = 0; j < 4; j++) {
				locations.add(new Location(tgtRabbit.y, tgtRabbit.x, j));
				getMovement(locations.get(j), tgtRabbit.d);
			}

			Collections.sort(locations);
			Location moveLocation = locations.get(0);

			tgtRabbit.y = moveLocation.y;
			tgtRabbit.x = moveLocation.x;
			tgtRabbit.jmpCount +=1;
			// 0, 0에서 시작하므로 그에 대한 보정값 2 추가
			tgtRabbit.currentScore += moveLocation.y + moveLocation.x + 2;
			totalScore += (moveLocation.y + moveLocation.x + 2);

			rabbitPriorityQueue.add(tgtRabbit);
			participatedRabbit.add(tgtRabbit);
			//System.out.println(tgtRabbit.pid + " " + tgtRabbit.currentScore);
		}

		Collections.sort(participatedRabbit, (o1, o2) -> {
			int compare = Integer.compare(o2.x + o2.y, o1.x + o1.y);
			if (compare == 0) {
				compare = Integer.compare(o2.y, o1.y);
				if (compare == 0) {
					compare = Integer.compare(o2.x, o1.x);
					if (compare == 0) {
						compare = Integer.compare(o2.pid, o1.pid);
					}
				}
			}

			return compare;
		});

		Rabbit bestRabbit = participatedRabbit.get(0);
		bestRabbit.rabbitScore += S;
	}

	// 위치를 계산한다.
	// 어느 위치에 있던 처음 이동 방향과 처음 위치로 되돌아 오는데는 (N - 1) * 2 번의 움직임 필요
	private static void getMovement(Location location, int distance) {
		int yMovement = dy[location.dir] * distance;
		int xMovement = dx[location.dir] * distance;

		yMovement %= ((N - 1) * 2);
		xMovement %= ((M - 1) * 2);

		// 각 끝단에서 바로 나가지는 방향으로의 이동은 바로 반대 방향으로 이동하는 것과 동치이다.
		if (location.y == 0 && yMovement < 0 || location.y == N - 1 && yMovement > 0) {
			yMovement *= - 1;
			location.dir = location.dir == 0 ? 2 : 0;
		}

		if (location.x == 0 && xMovement < 0 || location.x == M - 1 && xMovement > 0) {
			xMovement *= - 1;
			location.dir = location.dir == 1 ? 3 : 1;
		}

		// x
		if (location.x + xMovement >= M || location.x + xMovement < 0) {
			if(location.x + xMovement < 0) {
				xMovement *= - 1;
				xMovement -= location.x;
			} else {
				xMovement -= ((M - 1) - location.x); // 방향 바꿔야함
			}
			location.dir = location.dir == 1 ? 3 : 1;

			while(xMovement > M) {
				xMovement -= (M - 1);
				location.dir = location.dir == 1 ? 3 : 1;
			}

			if (location.dir == 3) {
				location.x = xMovement;
			} else {
				location.x = (M - 1) - xMovement;
			}
		} else {
			location.x = location.x + xMovement;
		}

		// y
		if (location.y + yMovement >= N || location.y + yMovement < 0) {
			if(location.y + yMovement < 0) {
				yMovement *= - 1;
				yMovement -= location.y;
			} else {
				yMovement -= ((N - 1) - location.y); // 방향 바꿔야함
			}
			location.dir = location.dir == 0 ? 2 : 0;

			while(yMovement > N) {
				yMovement -= (N - 1);
				location.dir = location.dir == 0 ? 2 : 0;
			}

			if (location.dir == 2) {
				location.y = yMovement;
			} else {
				location.y = (N - 1) - yMovement;
			}
		} else {
			location.y = location.y + yMovement;
		}
	}

	public static void changeDistance(int pid, int L) {
		rabbitMap.get(pid).d *= L;
	}

	public static long getBestRabbit() {
		PriorityQueue<Long> scoreQueue = new PriorityQueue<>(Collections.reverseOrder());

		for (int pid : rabbitMap.keySet()) {
			long score = totalScore - rabbitMap.get(pid).currentScore + rabbitMap.get(pid).rabbitScore;
			scoreQueue.add(score);
		}

		return scoreQueue.poll();
	}

}