import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class Main {

	public static class Knight {
		int y;
		int x;
		int height;
		int width;
		int health;
		int totalDamage;

		public Knight(int y, int x, int height, int width, int health) {
			this.x = x;
			this.y = y;
			this.height = height;
			this.width = width;
			this.health = health;
			this.totalDamage = 0;
		}
	}

	private static int[][] board;
	private static int L, N;
	private static Knight[] knights;
	private static int[] dy = {-1, 0, 1, 0};
	private static int[] dx = {0, 1, 0, -1};
	private static Set<Integer> pushedSet;

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());

		L = Integer.parseInt(st.nextToken());
		N = Integer.parseInt(st.nextToken());
		int Q = Integer.parseInt(st.nextToken());

		board = new int[L + 1][L + 1];
		for(int i = 1; i <= L; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 1; j <= L; j++) {
				board[i][j] = Integer.parseInt(st.nextToken());
			}
		}

		knights = new Knight[N + 1];
		for (int i = 1; i <= N; i++) {
			st = new StringTokenizer(br.readLine());

			int r = Integer.parseInt(st.nextToken());
			int c = Integer.parseInt(st.nextToken());
			int h = Integer.parseInt(st.nextToken());
			int w = Integer.parseInt(st.nextToken());
			int k = Integer.parseInt(st.nextToken());

			knights[i] = new Knight(r, c, h, w, k);
		}

		for(int i = 0; i < Q; i++) {
			st = new StringTokenizer(br.readLine());

			int idx = Integer.parseInt(st.nextToken());
			int dir = Integer.parseInt(st.nextToken());

			if(knights[idx].health <= 0) {
				continue;
			}

			pushedSet = new HashSet<>();
			boolean flag = check(idx, dir);

			if(!flag) {
				continue;
			}

			move(idx, dir);
			calcDamage();
		}

		int ans = 0;
		for(int i = 1; i <= N; i++) {
			if(knights[i].health <= 0) {
				continue;
			}
			ans += knights[i].totalDamage;
		}
		System.out.println(ans);
	}

	private static void move(int idx, int dir) {
		knights[idx].y += dy[dir];
		knights[idx].x += dx[dir];

		for(int element : pushedSet) {
			Knight knight = knights[element];

			knight.y += dy[dir];
			knight.x += dx[dir];
		}
	}

	private static void calcDamage() {
		for(int element : pushedSet) {
			Knight knight = knights[element];
			int cnt = 0;
			for(int y = knight.y; y < knight.y + knight.height; y++) {
				for(int x = knight.x; x < knight.x + knight.width; x++) {
					if(board[y][x] == 1) {
						cnt++;
					}
				}
			}
			knight.health = Math.max(0, knight.health - cnt);
			knight.totalDamage += cnt;
		}
	}

	private static boolean check(int idx, int dir) {
		Knight knight = knights[idx];

		int ny = knight.y + dy[dir];
		int nx = knight.x + dx[dir];

		for(int i = ny; i < ny + knight.height; i++) {
			for(int j = nx; j < nx + knight.width; j++) {
				if(i <= 0 || i > L || j <= 0 || j > L || board[i][j] == 2) {
					return false;
				}
				for(int k = 1; k <= N; k++) {
					if (k == idx) continue;

					if(!pushedSet.contains(k) && checkIsOverlap(i, j, k)) {
						pushedSet.add(k);
						boolean flag = check(k, dir);
						if(!flag) {
							return false;
						}
					}
				}
			}
		}
		//print();

		return true;
	}

	private static boolean checkIsOverlap(int y, int x, int k) {
		Knight knight = knights[k];
		if (knight.health <= 0) {
			return false;
		}

		for(int i = knight.y; i < knight.y + knight.height; i++) {
			for(int j = knight.x; j < knight.x + knight.width; j++) {
				if(y == i && x == j) {
					return true;
				}
			}
		}

		return false;
	}

}