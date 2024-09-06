import java.util.*;
import java.io.*;

public class Main {

	public static class Player {
		int y;
		int x;
		int dir;
		int status;
		int gunStatus;

		public Player(int y, int x, int dir, int status) {
			this.y = y;
			this.x = x;
			this.dir = dir;
			this.status = status;
			this.gunStatus = 0;
		}
	}

	private static int N, M, K;
	private static PriorityQueue<Integer>[][] board;
	private static Player[] players;
	private static int[] points;
	private static int[] dy = {-1, 0, 1, 0};
	private static int[] dx = {0, 1, 0, -1};

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());

		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		K = Integer.parseInt(st.nextToken());

		board = new PriorityQueue[N][N];
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				board[i][j] = new PriorityQueue<>((o1, o2) -> o2 - o1);
			}
		}

		for (int i = 0; i < N; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 0; j < N; j++) {
				int gunStatus = Integer.parseInt(st.nextToken());
				if (gunStatus > 0) {
					board[i][j].offer(gunStatus);
				}
			}
		}

		players = new Player[M];
		points = new int[M];
		for (int i = 0; i < M; i++) {
			st = new StringTokenizer(br.readLine());

			int y = Integer.parseInt(st.nextToken());
			int x = Integer.parseInt(st.nextToken());
			int dir = Integer.parseInt(st.nextToken());
			int status = Integer.parseInt(st.nextToken());

			players[i] = new Player(y - 1, x - 1, dir, status);
		}

		for (int i = 0; i < K; i++) {
			movePlayer();
		}

		StringBuilder sb = new StringBuilder();
		for (int element : points) {
			sb.append(element).append(" ");
		}
		System.out.println(sb);
	}

	private static void movePlayer() {
		for (int i = 0; i < players.length; i++) {
			int ny = players[i].y + dy[players[i].dir];
			int nx = players[i].x + dx[players[i].dir];

			if (ny < 0 || ny >= N || nx < 0 || nx >= N) {
				players[i].dir = (players[i].dir + 2) % 4;
				ny = players[i].y + dy[players[i].dir];
				nx = players[i].x + dx[players[i].dir];
			}
			players[i].y = ny;
			players[i].x = nx;

			boolean flag = false;
			for (int j = 0; j < players.length; j++) {
				if (i == j) continue;

				if (players[i].y == players[j].y && players[i].x == players[j].x) {
					flag = true;
					battle(i, j);
				}
			}

			if (!flag) {
				if (!board[players[i].y][players[i].x].isEmpty()) {
					if (players[i].gunStatus == 0) {
						players[i].gunStatus = board[players[i].y][players[i].x].poll();
					} else {
						if (players[i].gunStatus < board[players[i].y][players[i].x].peek()) {
							board[players[i].y][players[i].x].offer(players[i].gunStatus);
							players[i].gunStatus = board[players[i].y][players[i].x].poll();
						}
					}
				}
			}
		}
	}

	private static void battle(int playerId, int otherPlayerId) {
		int currentPlayerStatus = players[playerId].status + players[playerId].gunStatus;
		int otherPlayerStatus = players[otherPlayerId].status + players[otherPlayerId].gunStatus;

		if (currentPlayerStatus > otherPlayerStatus) {
			points[playerId] += (currentPlayerStatus - otherPlayerStatus);
			runOut(otherPlayerId);
			refresh(playerId);
		} else if (currentPlayerStatus < otherPlayerStatus) {
			points[otherPlayerId] += (otherPlayerStatus - currentPlayerStatus);
			runOut(playerId);
			refresh(otherPlayerId);
		} else {
			if (players[playerId].status < players[otherPlayerId].status) {
				runOut(playerId);
				refresh(otherPlayerId);
			} else {
				runOut(otherPlayerId);
				refresh(playerId);
			}
		}
	}

	private static void runOut(int playerId) {
		Player player = players[playerId];

		board[player.y][player.x].offer(player.gunStatus);
		player.gunStatus = 0;

		while(true) {
			boolean flag = false;
			int ny = player.y + dy[player.dir];
			int nx = player.x + dx[player.dir];

			if (ny < 0 || ny >= N || nx < 0 || nx >= N) {
				player.dir = (player.dir + 1) % 4;
				continue;
			}

			for (int i = 0; i < players.length; i++) {
				if (playerId == i) continue;
				if (players[i].y == ny && players[i].x == nx) {
					flag = true;
					break;
				}
			}

			if (!flag) {
				player.y = ny;
				player.x = nx;
				break;
			}
		}

		if (!board[player.y][player.x].isEmpty()) {
			player.gunStatus += board[player.y][player.x].poll();
		}
	}

	private static void refresh(int playerId) {
		Player player = players[playerId];
		if (player.gunStatus < board[player.y][player.x].peek()) {
			board[player.y][player.x].offer(player.gunStatus);
			player.gunStatus = board[player.y][player.x].poll();
		}
	}
}