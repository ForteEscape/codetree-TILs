import java.util.*;
import java.io.*;

public class Main {

	public static class Santa {
		int y;
		int x;
		boolean isStun;
		boolean isFailed;
		int stunFinishedTurn;
		int score;

		public Santa(int y, int x, boolean isStun, boolean isFailed) {
			this.y = y;
			this.x = x;
			this.isFailed = isFailed;
			this.isStun = isStun;
			this.score = 0;
			this.stunFinishedTurn = -1;
		}
	}

	private static int N, M, P, C, D;
	private static Santa rudolf = null;
	private static Santa[] santas;
	private static int[] rudolfDy = {-1, -1, 0, 1, 1, 1, 0, -1};
	private static int[] rudolfDx = {0, 1, 1, 1, 0, -1, -1, -1};
	private static int[] santaDy = {-1, 0, 1, 0};
	private static int[] santaDx = {0, 1, 0, -1};

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());

		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		P = Integer.parseInt(st.nextToken());
		C = Integer.parseInt(st.nextToken());
		D = Integer.parseInt(st.nextToken());

		santas = new Santa[P];

		st = new StringTokenizer(br.readLine());
		int rudolfY = Integer.parseInt(st.nextToken());
		int rudolfX = Integer.parseInt(st.nextToken());
		rudolf = new Santa(rudolfY, rudolfX, false, false);

		for (int i = 0; i < P; i++) {
			st = new StringTokenizer(br.readLine());

			int num = Integer.parseInt(st.nextToken());
			int y = Integer.parseInt(st.nextToken());
			int x = Integer.parseInt(st.nextToken());

			santas[num - 1] = new Santa(y, x, false, false);
		}

		int currentTurn = 1;
		while (currentTurn <= M) {
			resolveStun(currentTurn);
			moveRudolf(currentTurn);
			moveSanta(currentTurn);

			for(int i = 0; i < P; i++) {
				if(santas[i].isFailed) continue;
				santas[i].score += 1;
			}

			currentTurn++;
		}

		StringBuilder ans = new StringBuilder();
		for(int i = 0; i < P; i++) {
			ans.append(santas[i].score).append(" ");
		}
		System.out.println(ans);
	}

	private static void moveRudolf(int currentTurn) {
		List<int[]> distList = new ArrayList<>();

		for (int i = 0; i < P; i++) {
			if (santas[i].isFailed)
				continue;

			int dist = calcDist(i);
			distList.add(new int[] {i, dist});
		}

		Collections.sort(distList, (o1, o2) -> {
			int res = Integer.compare(o1[1], o2[1]);

			if (res == 0) {
				res = Integer.compare(santas[o2[0]].y, santas[o1[0]].y);
				if (res == 0) {
					return Integer.compare(santas[o2[0]].x, santas[o1[0]].x);
				}
			}

			return res;
		});

		PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(o -> o[2]));
		for (int i = 0; i < 8; i++) {
			int ny = rudolf.y + rudolfDy[i];
			int nx = rudolf.x + rudolfDx[i];

			if (isUnreachable(ny, nx))
				continue;

			int nextDist = calcDistFromLocation(ny, nx, distList.get(0)[0]);
			pq.offer(new int[] {ny, nx, nextDist, i});
		}

		int[] result = pq.poll();
		rudolf.y = result[0];
		rudolf.x = result[1];

		for (int i = 0; i < P; i++) {
			if (rudolf.y == santas[i].y && rudolf.x == santas[i].x) {
				collision(i, result[3], true, currentTurn);
			}
		}
	}

	private static int calcDist(int i) {
		return (rudolf.y - santas[i].y) * (rudolf.y - santas[i].y) +
			((rudolf.x - santas[i].x)) * (rudolf.x - santas[i].x);
	}

	private static void moveSanta(int currentTurn) {
		for (int i = 0; i < P; i++) {
			if (santas[i].isFailed || santas[i].isStun)
				continue;
			move(i, currentTurn);
		}
	}

	private static void move(int num, int currentTurn) {
		PriorityQueue<int[]> pq = new PriorityQueue<>((o1, o2) -> {
			int res = Integer.compare(o1[2], o2[2]);

			if (res == 0) {
				return Integer.compare(o1[3], o2[3]);
			}

			return res;
		});

		int currentDist = calcDist(num);

		for (int i = 0; i < 4; i++) {
			int ny = santas[num].y + santaDy[i];
			int nx = santas[num].x + santaDx[i];

			int nextDist = calcDistFromLocation(ny, nx);
			if (isUnreachable(ny, nx) || currentDist <= nextDist || isExists(ny, nx, num))
				continue;

			pq.offer(new int[] {ny, nx, nextDist, i});
		}

		if (pq.isEmpty()) {
			return;
		}

		int[] res = pq.poll();
		santas[num].y = res[0];
		santas[num].x = res[1];

		if(santas[num].y == rudolf.y && santas[num].x == rudolf.x) {
			collision(num, res[3], false, currentTurn);
		}
	}

	private static int calcDistFromLocation(int y, int x) {
		return (rudolf.y - y) * (rudolf.y - y) + ((rudolf.x - x)) * (rudolf.x - x);
	}

	private static int calcDistFromLocation(int y, int x, int num) {
		return (santas[num].y - y) * (santas[num].y - y) + ((santas[num].x - x)) * (santas[num].x - x);
	}

	private static boolean isExists(int y, int x, int num) {
		for (int i = 0; i < P; i++) {
			if (i == num || santas[i].isFailed)
				continue;

			if (santas[i].y == y && santas[i].x == x) {
				return true;
			}
		}
		return false;
	}

	private static void collision(int num, int dir, boolean causeRudolf, int currentTurn) {
		if (causeRudolf) {
			santas[num].score += C;

			int ny = santas[num].y + (rudolfDy[dir] * C);
			int nx = santas[num].x + (rudolfDx[dir] * C);

			santas[num].y = ny;
			santas[num].x = nx;
		} else {
			santas[num].score += D;

			dir = (dir + 2) % 4;
			int ny = santas[num].y + (santaDy[dir] * D);
			int nx = santas[num].x + (santaDx[dir] * D);

			santas[num].y = ny;
			santas[num].x = nx;
		}

		if (isUnreachable(santas[num].y, santas[num].x)) {
			santas[num].isFailed = true;
			return;
		}

		for (int i = 0; i < P; i++) {
			if (i == num || santas[i].isFailed)
				continue;

			if (santas[i].y == santas[num].y && santas[i].x == santas[num].x) {
				interaction(i, dir, causeRudolf);
			}
		}

		santas[num].isStun = true;
		santas[num].stunFinishedTurn = currentTurn + 2;
	}

	private static void interaction(int num, int dir, boolean causeRudolf) {
		if (causeRudolf) {
			int ny = santas[num].y + rudolfDy[dir];
			int nx = santas[num].x + rudolfDx[dir];

			santas[num].y = ny;
			santas[num].x = nx;
		} else {
			int ny = santas[num].y + santaDy[dir];
			int nx = santas[num].x + santaDx[dir];

			santas[num].y = ny;
			santas[num].x = nx;
		}

		if (isUnreachable(santas[num].y, santas[num].x)) {
			santas[num].isFailed = true;
			return;
		}

		for (int i = 0; i < P; i++) {
			if (i == num || santas[i].isFailed)
				continue;

			if (santas[i].y == santas[num].y && santas[i].x == santas[num].x) {
				interaction(i, dir, causeRudolf);
			}
		}
	}

	private static boolean isUnreachable(int y, int x) {
		return y < 1 || y > N || x < 0 || x > N;
	}

	private static void resolveStun(int currentTurn) {
		for(int i = 0; i < P; i++) {
			if (santas[i].isFailed) continue;

			if(santas[i].isStun && santas[i].stunFinishedTurn == currentTurn) {
				santas[i].isStun = false;
				santas[i].stunFinishedTurn = -1;
			}
		}
	}

}