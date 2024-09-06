import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class Main {

	public static class Tower {
		int atk;
		int y;
		int x;
		int attackCnt;

		public Tower(int atk, int y, int x) {
			this.atk = atk;
			this.y = y;
			this.x = x;
			this.attackCnt = 0;
		}
	}

	public static class Location {
		int y;
		int x;
		List<Location> trace;

		public Location(int y, int x) {
			this.y = y;
			this.x = x;
			this.trace = null;
		}
	}

	private static int N, M, K;
	private static Tower[][] towers;
	private static List<Tower> attackerTowerList, attackedTowerList;
	private static List<Location> attackTraceList;
	private static boolean[][] visited;
	private static Set<Tower> peacefulTower;

	private static final int[] dy = {0, 1, 0, -1};
	private static final int[] dx = {1, 0, -1, 0};
	private static final int[] dy2 = {-1, -1, 0, 1, 1, 1, 0, -1};
	private static final int[] dx2 = {0, 1, 1, 1, 0, -1, -1, -1};

	public static void main(String[] args) throws IOException {
		init();
		for (int turn = 0; turn < K; turn++) {
			initTower();
			if(attackerTowerList.size() == 1) {
				break;
			}
			Tower attacker = selectAttacker();
			Tower target = selectTarget();

			attacker.atk += (N + M);
			attacker.attackCnt = 0;

			peacefulTower.remove(attacker);
			for (Tower tower : peacefulTower) {
				tower.attackCnt++;
			}
			peacefulTower.remove(target);

			attackTraceList = new ArrayList<>();
			visited = new boolean[N][M];

			attackTraceList = isReachable(attacker.y, attacker.x, target);

			if (attackTraceList.isEmpty()) {
				attackBomb(target, attacker.atk);
			} else {
				attackLaser(attacker.atk);
			}

			restoreTower();
		}

		int maxAtk = Integer.MIN_VALUE;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				maxAtk = Math.max(maxAtk, towers[i][j].atk);
			}
		}
		System.out.println(maxAtk);
	}

	private static void print() {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				System.out.print(towers[i][j].atk + " ");
			}
			System.out.println();
		}
		System.out.println("=====");
	}

	private static void initTower() {
		attackerTowerList = new ArrayList<>();
		attackedTowerList = new ArrayList<>();
		peacefulTower = new HashSet<>();

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				if (towers[i][j].atk > 0) {
					attackerTowerList.add(towers[i][j]);
					attackedTowerList.add(towers[i][j]);
					peacefulTower.add(towers[i][j]);
				}
			}
		}
	}

	private static void restoreTower() {
		for (Tower peacefulTowerElement : peacefulTower) {
			peacefulTowerElement.atk += 1;
		}
	}

	private static void attackBomb(Tower target, int attackerAtk) {
		towers[target.y][target.x].atk -= attackerAtk;

		if (towers[target.y][target.x].atk <= 0) {
			towers[target.y][target.x].atk = 0;
		}

		for (int i = 0; i < 8; i++) {
			int ny = target.y + dy2[i];
			int nx = target.x + dx2[i];

			if (ny >= N) {
				ny = ny % N;
			} else if (ny < 0) {
				ny = (ny + N) % N;
			}

			if (nx >= M) {
				nx = nx % M;
			} else if (nx < 0) {
				nx = (nx + M) % M;
			}

			towers[ny][nx].atk -= (attackerAtk / 2);
			peacefulTower.remove(towers[ny][nx]);
			if (towers[ny][nx].atk < 0) {
				towers[ny][nx].atk = 0;
			}
		}
	}

	private static void attackLaser(int attackerAtk) {
		for (int i = 1; i < attackTraceList.size(); i++) {
			Location location = attackTraceList.get(i);

			peacefulTower.remove(towers[location.y][location.x]);

			if (i == attackTraceList.size() - 1) {
				towers[location.y][location.x].atk -= attackerAtk;
				if (towers[location.y][location.x].atk < 0) {
					towers[location.y][location.x].atk = 0;
				}
				continue;
			}
			towers[location.y][location.x].atk -= (attackerAtk / 2);
			if (towers[location.y][location.x].atk < 0) {
				towers[location.y][location.x].atk = 0;
			}
		}
	}

	private static List<Location> isReachable(int y, int x, Tower target) {
		Deque<Location> queue = new ArrayDeque<>();
		visited[y][x] = true;

		Location start = new Location(y, x);
		start.trace = new ArrayList<>();

		queue.add(start);

		while (!queue.isEmpty()) {
			Location location = queue.poll();

			if (location.y == target.y && location.x == target.x) {
				location.trace.add(new Location(target.y, target.x));
				return location.trace;
			}

			for (int i = 0; i < 4; i++) {
				int ny = location.y + dy[i];
				int nx = location.x + dx[i];

				if (ny >= N) {
					ny = ny % N;
				} else if (ny < 0) {
					ny = (ny + N) % N;
				}

				if (nx >= M) {
					nx = nx % M;
				} else if (nx < 0) {
					nx = (nx + M) % M;
				}

				if (towers[ny][nx].atk == 0 || visited[ny][nx]) {
					continue;
				}

				visited[ny][nx] = true;
				Location next = new Location(ny, nx);
				next.trace = new ArrayList<>(location.trace);
				next.trace.add(location);

				queue.addLast(next);
			}
		}

		return new ArrayList<>();
	}

	private static Tower selectAttacker() {
		Collections.sort(attackerTowerList, (t1, t2) -> {
			int compare = Integer.compare(t1.atk, t2.atk);
			if(compare == 0) {
				compare = Integer.compare(t1.attackCnt, t2.attackCnt);
				if (compare == 0) {
					compare = Integer.compare((t2.y + t2.x), (t1.y + t1.x));
					if (compare == 0) {
						return Integer.compare(t2.x, t1.x);
					}
				}
			}
			return compare;
		});

		return attackerTowerList.get(0);
	}

	private static Tower selectTarget() {
		Collections.sort(attackedTowerList, (t1, t2) -> {
			int compare = Integer.compare(t2.atk, t1.atk);
			if(compare == 0) {
				compare = Integer.compare(t2.attackCnt, t1.attackCnt);
				if (compare == 0) {
					compare = Integer.compare((t1.y + t1.x), (t2.y + t2.x));
					if (compare == 0) {
						return Integer.compare(t1.x, t2.x);
					}
				}
			}
			return compare;
		});

		return attackedTowerList.get(0);
	}

	public static void init() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());

		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		K = Integer.parseInt(st.nextToken());

		towers = new Tower[N][M];

		for(int i = 0; i < N; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 0; j < M; j++) {
				int atk = Integer.parseInt(st.nextToken());
				Tower tower = new Tower(atk, i, j);
				towers[i][j] = tower;
			}
		}
	}
}