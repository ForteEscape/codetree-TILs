import java.util.*;
import java.io.*;

public class Main {

	public static class Player {
		int y;
		int x;
		boolean reached = false;

		Player(int y, int x) {
			this.y = y;
			this.x = x;
			this.reached = false;
		}
	}

	public static class Location {
		int y;
		int x;
		List<int[]> baseCampLocation;

		public Location(int y, int x) {
			this.y = y;
			this.x = x;
			this.baseCampLocation = null;
		}
	}

	private static int N, M;
	private static int[][] board;
	private static boolean[][] visited;
	private static Location[] store;
	private static int[] dy = {-1, 0, 0, 1};
	private static int[] dx = {0, -1, 1, 0};
	private static List<Player> moveablePlayers;

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());

		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());

		board = new int[N + 1][N + 1];

		for (int i = 1; i <= N; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 1; j <= N; j++) {
				board[i][j] = Integer.parseInt(st.nextToken());
			}
		}

		store = new Location[M + 1];
		for (int i = 1; i <= M; i++) {
			st = new StringTokenizer(br.readLine());

			int y = Integer.parseInt(st.nextToken());
			int x = Integer.parseInt(st.nextToken());
			store[i] = new Location(y, x);
		}

		// for (int i = 1; i <= M; i++) {
		// 	System.out.println(i + " : " + Arrays.toString(store[i].baseCampLocation.get(0)));
		// }

		moveablePlayers = new ArrayList<>();
		visited = new boolean[N + 1][N + 1];

		int time = 1;
		while(true) {
			//System.out.println(time);
			int num = 1;
			for (Player p : moveablePlayers) {
				if (p.reached) {
					num++;
					continue;
				}
				//System.out.println("player " + num + "'s goal : (" + store[num].y + ", " + store[num].x + ")");
				//System.out.println("player " + num + "'s location : (" + p.y + ", " + p.x + ")");
				move(p, num);
				//System.out.println("player " + num + "'s after location : (" + p.y + ", " + p.x + ")");
				num++;
			}

			// printVisit();
			checkArrive();

			if (time <= M) {
				// 배정할때 기준으로 잡아야함
				for (int i = 1; i <= M; i++) {
					searchBaseCampLocation(store[i].y, store[i].x, i);
				}

				for (int[] element : store[time].baseCampLocation) {
					if (!visited[element[0]][element[1]]) {
						visited[element[0]][element[1]] = true;
						moveablePlayers.add(new Player(element[0], element[1]));
						break;
					}
				}
			}

			if(checkAllArrived()) {
				// System.out.println("all arrived");
				break;
			}

			time++;

			// System.out.println("====");
		}

		System.out.println(time);
	}

	private static void printVisit() {
		for (int i = 1; i <= N; i++) {
			for (int j = 1; j <= N; j++) {
				System.out.print(visited[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("========");
	}

	private static boolean checkAllArrived() {
		if (moveablePlayers.isEmpty()) {
			return false;
		}

		for (Player p : moveablePlayers) {
			if (!p.reached) {
				return false;
			}
		}
		return true;
	}

	private static void checkArrive() {
		int idx = 1;
		for (Player p : moveablePlayers) {
			if (p.reached) {
				idx++;
				continue;
			}

			if (store[idx].y == p.y && store[idx].x == p.x) {
				// System.out.println("player " + idx + " has arrived");
				p.reached = true;
				visited[p.y][p.x] = true;
			}
			idx++;
		}
	}

	private static void move(Player p, int num) {
		List<int[]> moveableLocationList = new ArrayList<>();
		// System.out.println("num : " + num + " " + p.y + " " + p.x);

		for (int i = 0; i < 4; i++) {
			int ny = p.y + dy[i];
			int nx = p.x + dx[i];

			if (isUnreachable(ny, nx) || visited[ny][nx]) {
				continue;
			}

			int dist = checkDistance(ny, nx, num);
			moveableLocationList.add(new int[]{ny, nx, dist, i});
		}

		Collections.sort(moveableLocationList, (o1, o2) -> {
			int res = Integer.compare(o1[2], o2[2]);
			if (res == 0) {
				res = Integer.compare(o1[3], o2[3]);
			}
			return res;
		});

		p.y = moveableLocationList.get(0)[0];
		p.x = moveableLocationList.get(0)[1];
	}

	private static int checkDistance(int y, int x, int num) {
		boolean[][] visited2 = copyFromVisited();
		Deque<int[]> queue = new ArrayDeque<>();

		queue.add(new int[]{y, x, 0});
		visited2[y][x] = true;

		while(!queue.isEmpty()) {
			int[] cur = queue.pollFirst();

			if (store[num].y == cur[0] && store[num].x == cur[1]) {
				return cur[2];
			}

			for (int i = 0; i < 4; i++) {
				int ny = cur[0] + dy[i];
				int nx = cur[1] + dx[i];

				if (isUnreachable(ny, nx) || visited2[ny][nx]) {
					continue;
				}

				visited2[ny][nx] = true;
				queue.addLast(new int[]{ny, nx, cur[2] + 1});
			}
		}

		return Integer.MAX_VALUE;
	}

	private static boolean[][] copyFromVisited() {
		boolean[][] temp = new boolean[N + 1][N + 1];

		for (int i = 1; i <= N; i++) {
			for (int j = 1; j <= N; j++) {
				temp[i][j] = visited[i][j];
			}
		}

		return temp;
	}

	private static void searchBaseCampLocation(int y, int x, int num) {
		boolean[][] visited2 = copyFromVisited();
		Deque<int[]> queue = new ArrayDeque<>();

		visited2[y][x] = true;
		queue.addLast(new int[]{y, x, 0});

		store[num].baseCampLocation = new ArrayList<>();
		while(!queue.isEmpty()) {
			int[] cur = queue.pollFirst();

			if (board[cur[0]][cur[1]] == 1) {
				store[num].baseCampLocation.add(new int[]{cur[0], cur[1], cur[2]});
			}

			for (int i = 0; i < 4; i++) {
				int ny = cur[0] + dy[i];
				int nx = cur[1] + dx[i];

				if (isUnreachable(ny, nx) || visited2[ny][nx]) {
					continue;
				}

				visited2[ny][nx] = true;
				queue.addLast(new int[]{ny, nx, cur[2] + 1});
			}
		}

		Collections.sort(store[num].baseCampLocation, (o1, o2) -> {
			int res = Integer.compare(o1[2], o2[2]);
			if (res == 0) {
				res = Integer.compare(o1[0], o2[0]);
				if (res == 0) {
					res = Integer.compare(o1[1], o2[1]);
				}
			}
			return res;
		});
	}

	private static boolean isUnreachable(int y, int x) {
		return y < 1 || y > N || x < 1 || x > N;
	}
}