import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

public class Main {

	public static class RotateOrder implements Comparable<RotateOrder> {
		int relicCount;
		int y;
		int x;
		int rotateCnt;

		public RotateOrder(int relicCount, int y, int x, int rotateCnt) {
			this.relicCount = relicCount;
			this.y = y;
			this.x = x;
			this.rotateCnt = rotateCnt;
		}

		@Override
		public int compareTo(RotateOrder o) {
			int compare = Integer.compare(o.relicCount, this.relicCount);
			if (compare == 0) {
				compare = Integer.compare(this.rotateCnt, o.rotateCnt);
				if (compare == 0) {
					compare = Integer.compare(this.x, o.x);
					if (compare == 0) {
						return Integer.compare(this.y, o.y);
					}
				}
			}
			return compare;
		}

	}

	private static int K, M;
	private static int[][] board, tempBoard;
	private static boolean[][] visited;
	private static int[] relicInfo;
	private static List<Integer> answerList;
	private static int[] dy = {-1, 0, 1, 0};
	private static int[] dx = {0, 1, 0, -1};
	private static int nextRelicIdx = 0;

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());

		K = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());

		board = new int[5][5];
		relicInfo = new int[M];
		answerList = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 0; j < 5; j++) {
				board[i][j] = Integer.parseInt(st.nextToken());
			}
		}

		st = new StringTokenizer(br.readLine());
		for (int i = 0; i < M; i++) {
			relicInfo[i] = Integer.parseInt(st.nextToken());
		}

		while (K > 0) {
			// 5x5 의 중앙 부분 3x3을 하나씩 선택하여 돌린다.
			PriorityQueue<RotateOrder> pq = new PriorityQueue<>();

			for (int i = 1; i <= 3; i++) {
				for (int j = 1; j <= 3; j++) {
					for (int k = 1; k <= 3; k++) {
						init();
						rotateArray(i, j, k);

						int firstRelicCount = calcRelic().size();
						pq.offer(new RotateOrder(firstRelicCount, i, j, k));
					}
				}
			}

			RotateOrder tgt = pq.peek();
			if (tgt.relicCount == 0) {
				break;
			}

			init();
			rotateArray(tgt.y, tgt.x, tgt.rotateCnt);
			List<int[]> needReconstructList = calcRelic();

			int ans = tgt.relicCount;
			reconstructRelic(needReconstructList);

			while (true) {
				needReconstructList = calcRelic();

				if (needReconstructList.isEmpty()) {
					break;
				}

				ans += needReconstructList.size();
				reconstructRelic(needReconstructList);
			}

			answerList.add(ans);
			apply();

			K--;
		}

		StringBuilder sb = new StringBuilder();
		for (int relicCount : answerList) {
			sb.append(relicCount + " ");
		}
		System.out.print(sb);
	}

	/*
	5x5 사이즈 배열에서 3x3 배열을 선택해 90, 180, 270 중 하나를 선택하여 돌린다.
	이때 반드시 3x3이 되어야 하므로 5x5에서 이를 구하는 경우는 실제로는 3x3이다.
	 */
	// 임시 board 제작하는 메서드
	private static void init() {
		tempBoard = new int[5][5];

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				tempBoard[i][j] = board[i][j];
			}
		}
	}

	private static void apply() {
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				board[i][j] = tempBoard[i][j];
			}
		}
	}

	// 중심 좌표를 입력받아 3x3 배열을 회전시키는 메서드
	private static void rotateArray(int y, int x, int rotateCount) {
		int[][] subBoard = getSubBoard(y, x);
		for (int i = 0; i < rotateCount; i++) {
			rotate(subBoard);
		}
		applySubBoard(y, x, subBoard);
	}

	private static int[][] getSubBoard(int y, int x) {
		int[][] subBoard = new int[3][3];
		for (int i = y - 1, row = 0; i <= y + 1; i++, row++) {
			for (int j = x - 1, col = 0; j <= x + 1; j++, col++) {
				subBoard[row][col] = tempBoard[i][j];
			}
		}
		return subBoard;
	}

	private static void rotate(int[][] subBoard) {
		int n = subBoard.length;
		int[][] tmpArray = new int[3][3];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				tmpArray[j][n - 1 - i] = subBoard[i][j];
			}
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				subBoard[i][j] = tmpArray[i][j];
			}
		}
	}

	private static void applySubBoard(int y, int x, int[][] subBoard) {
		for (int i = y - 1, row = 0; i <= y + 1; i++, row++) {
			for (int j = x - 1, col = 0; j <= x + 1; j++, col++) {
				tempBoard[i][j] = subBoard[row][col];
			}
		}
	}

	// 얻을 수 있는 유물의 수 카운트
	private static List<int[]> calcRelic() {
		visited = new boolean[5][5];
		List<int[]> removedList = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (!visited[i][j]) {
					List<int[]> result = getConnectedCount(i, j);

					if (result.size() > 2) {
						removeRelic(result);
						removedList.addAll(result);
					}
				}
			}
		}

		return removedList;
	}

	private static void removeRelic(List<int[]> result) {
		for (int[] element : result) {
			tempBoard[element[0]][element[1]] = 0;
		}
	}

	private static List<int[]> getConnectedCount(int y, int x) {
		List<int[]> result = new ArrayList<>();
		visited[y][x] = true;

		Deque<int[]> queue = new ArrayDeque<>();
		queue.addLast(new int[] {y, x});
		result.add(new int[] {y, x});
		int key = tempBoard[y][x];

		while (!queue.isEmpty()) {
			int[] cur = queue.pollFirst();

			for (int dir = 0; dir < 4; dir++) {
				int ny = cur[0] + dy[dir];
				int nx = cur[1] + dx[dir];

				if (isUnreachable(ny, nx) || tempBoard[ny][nx] != key || visited[ny][nx]) {
					continue;
				}

				visited[ny][nx] = true;
				queue.addLast(new int[] {ny, nx});
				result.add(new int[] {ny, nx});
			}
		}

		return result;
	}

	private static boolean isUnreachable(int y, int x) {
		return y < 0 || y >= 5 || x < 0 || x >= 5;
	}

	private static void reconstructRelic(List<int[]> removedList) {
		Collections.sort(removedList, (o1, o2) -> o1[1] == o2[1] ? o2[0] - o1[0] : o1[1] - o2[1]);
		for (int[] element : removedList) {
			tempBoard[element[0]][element[1]] = relicInfo[nextRelicIdx++];
		}
	}

}