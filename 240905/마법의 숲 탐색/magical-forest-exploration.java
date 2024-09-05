import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.StringTokenizer;

public class Main {

	public static class Golem {
		int id;
		int y;
		int x;
		int dir;
		int[] exitPoint;

		public Golem(int id, int y, int x, int dir) {
			this.id = id;
			this.y = y;
			this.x = x;
			this.dir = dir;

			exitPoint = new int[]{y + dy[dir], x + dx[dir]};
		}
	}

	private static int R, C, K;
	private static int[][] board;
	private static boolean[][] visited;
	private static int[] dy = {-1, 0, 1, 0};
	private static int[] dx = {0, 1, 0, -1};
	private static Golem[] golems;

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());

		R = Integer.parseInt(st.nextToken());
		C = Integer.parseInt(st.nextToken());
		K = Integer.parseInt(st.nextToken());

		board = new int[R + 3][C + 1];
		golems = new Golem[K + 1];

		int ans = 0;
		for (int i = 1; i <= K; i++) {
			st = new StringTokenizer(br.readLine());

			int startCol = Integer.parseInt(st.nextToken());
			int startRow = 1;
			int startDir = Integer.parseInt(st.nextToken());

			int currentRow = moveDown(startCol, startRow);
			int currentCol = startCol;
			int currentDir = startDir;

			while(true) {
				boolean flag = false;

				if(isMoveableToLeft(currentCol, currentRow)) { // 좌측 무빙 가능하다면
					currentCol--;
					currentRow++;
					currentDir = ((currentDir - 1) + 4) % 4;

					flag = true;
				} else if(isMoveableToRight(currentCol, currentRow)) { // 우측 무빙 가능하다면
					currentCol++;
					currentRow++;
					currentDir = (currentDir + 1) % 4;

					flag = true;
				}

				if (!flag) break;
			}

			if(isOutSide(currentCol, currentRow)) {
				reset();
				continue;
			}

			apply(i, currentRow, currentCol);
			golems[i] = new Golem(i, currentRow, currentCol, currentDir);

			int result = bfs(currentRow, currentCol, i);
			ans += result;
		}

		System.out.println(ans);
	}

	private static int bfs(int y, int x, int id) {
		visited = new boolean[R + 3][C + 1];
		visited[y][x] = true;

		Deque<int[]> queue = new ArrayDeque<>();
		queue.addLast(new int[]{y, x, id});

		int maxY = Integer.MIN_VALUE;

		while(!queue.isEmpty()) {
			int[] cur = queue.pollFirst();

			maxY = Math.max(maxY, cur[0]);

			for(int i = 0; i < 4; i++) {
				int ny = cur[0] + dy[i];
				int nx = cur[1] + dx[i];

				if(ny < 0 || ny >= R + 3 || nx <= 0 || nx > C || visited[ny][nx]) {
					continue;
				}

				if(board[ny][nx] != cur[2]) {
					if(board[ny][nx] == 0) continue;
					if(golems[cur[2]].exitPoint[0] != cur[0] || golems[cur[2]].exitPoint[1] != cur[1]) {
						continue;
					}
				}

				visited[ny][nx] = true;
				queue.addLast(new int[]{ny, nx, board[ny][nx]});
			}
		}

		return maxY - 2;
	}

	private static void apply(int id, int currentRow, int currentCol) {
		board[currentRow][currentCol] = id;
		for(int i = 0; i < 4; i++) {
			int ny = currentRow + dy[i];
			int nx = currentCol + dx[i];

			board[ny][nx] = id;
		}
	}

	private static void reset() {
		for(int i = 0; i < board.length; i++) {
			for(int j = 0; j < board[i].length; j++) {
				board[i][j] = 0;
			}
		}
	}

	private static boolean isOutSide(int currentCol, int currentRow) {
		if(currentRow < 3) {
			return true;
		}

		for(int i = 0; i < 4; i++) {
			int ny = currentRow + dy[i];
			int nx = currentCol + dx[i];

			if(ny < 3) return true;
		}

		return false;
	}

	private static boolean isMoveableToLeft(int startCol, int currentRow) {
		int baseCol = startCol - 1;
		int baseRow = currentRow;

		for(int i = 4; i > 1; i--) {
			int ny = baseRow + dy[i % 4];
			int nx = baseCol + dx[i % 4];

			if(ny >= R + 3 || nx <= 0) {
				return false;
			}

			if(board[ny][nx] != 0) {
				return false;
			}
		}

		baseRow = currentRow + 1;
		for(int i = 3; i > 1; i--) {
			int ny = baseRow + dy[i];
			int nx = baseCol + dx[i];

			if(ny >= R + 3 || nx <= 0) {
				return false;
			}

			if(board[ny][nx] != 0) {
				return false;
			}
		}

		return true;
	}

	private static boolean isMoveableToRight(int startCol, int currentRow) {
		int baseCol = startCol + 1;
		int baseRow = currentRow;

		for(int i = 0; i < 3; i++) {
			int ny = baseRow + dy[i];
			int nx = baseCol + dx[i];

			if(ny >= R + 3 || nx > C) {
				return false;
			}

			if(board[ny][nx] != 0) {
				return false;
			}
		}

		baseRow = currentRow + 1;
		for(int i = 1; i < 3; i++) {
			int ny = baseRow + dy[i];
			int nx = baseCol + dx[i];

			if(ny >= R + 3 || nx > C) {
				return false;
			}

			if(board[ny][nx] != 0) {
				return false;
			}
		}

		return true;
	}

	private static int moveDown(int startCol, int startRow) {
		int currentRow = startRow;

		while(currentRow < R + 3 && isApplyableDown(startCol, currentRow)) {
			currentRow++;
		}

		return currentRow;
	}

	private static boolean isApplyableDown(int startCol, int currentRow) {
		int baseCol = startCol;
		int baseRow = currentRow + 1;

		for(int i = 1; i < 4; i++) {
			int ny = baseRow + dy[i];
			int nx = baseCol + dx[i];

			if(ny >= R + 3 || nx <= 0 || nx > C) {
				return false;
			}

			if(board[ny][nx] != 0) {
				return false;
			}
		}

		return true;
	}

}