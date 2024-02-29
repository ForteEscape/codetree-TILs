import java.io.*;
import java.util.*;

public class Main {
	
	static class Location implements Comparable<Location> {
		int studentNum;
		int y;
		int x;
		int emptyCnt;
		int likeCnt;
		
		Location(int studentNum, int y, int x, int emptyCnt, int likeCnt) {
			this.studentNum = studentNum;
			this.y = y;
			this.x = x;
			this.emptyCnt = emptyCnt;
			this.likeCnt = likeCnt;
		}
		
		@Override
		public int compareTo(Location o) {
			if(o.likeCnt == this.likeCnt) {
				if(o.emptyCnt == this.emptyCnt) {
					if(this.y == o.y) {
						return this.x - o.x;
					}
					return this.y - o.y;
				}
				return o.emptyCnt - this.emptyCnt;
			}
			return o.likeCnt - this.likeCnt;
		}
	}
	
	static int[][] board;
	static int N;
	static Set<Integer>[] set;
	static int[] dy = {-1, 0, 1, 0};
	static int[] dx = {0, 1, 0, -1};
	static PriorityQueue<Location> pq;
	
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st;
		N = Integer.parseInt(br.readLine());
		
		board = new int[N][N];
		set = new Set[N * N + 1];
		for(int i = 0; i <= N * N; i++) {
			set[i] = new HashSet<>();
		}
		
		List<Location> ans = new ArrayList<>();
		
		for(int i = 0; i < N * N; i++) {
			st = new StringTokenizer(br.readLine());
			
			int currentStudent = Integer.parseInt(st.nextToken());
			for(int j = 0; j < 4; j++) {
				set[currentStudent].add(Integer.parseInt(st.nextToken()));
			}
			
			pq = new PriorityQueue<>();
			check(currentStudent);
			
			Location result = pq.poll();
			board[result.y][result.x] = currentStudent;
			
			ans.add(result);
		}
		
		int result = 0;
		for(Location location : ans) {
			int temp = 0;
			
			for(int i = 0; i < 4; i++) {
				int ny = location.y + dy[i];
				int nx = location.x + dx[i];
				
				if(isUnreachable(ny, nx)) {
					continue;
				}
				
				if(set[location.studentNum].contains(board[ny][nx])) {
					temp++;
				}
			}
			
			
			result += temp == 0 ? 0 : (int) Math.pow(10, temp - 1);
		}
		
		System.out.println(result);
	}
	
	public static void check(int currentStudent) {
		for(int i = 0; i < N; i++) {
			for(int j = 0; j < N; j++) {
				
				if(board[i][j] != 0) {
					continue;
				}
				
				Location curLocation = new Location(currentStudent, i, j, 0, 0);
				
				for(int k = 0; k < 4; k++) {
					int ny = i + dy[k];
					int nx = j + dx[k];
					
					if(isUnreachable(ny, nx)) {
						continue;
					}
					
					if(board[ny][nx] == 0) {
						curLocation.emptyCnt++;
					} else {
						if(set[currentStudent].contains(board[ny][nx])) {
							curLocation.likeCnt++;
						}
					}
				}
				
				pq.offer(curLocation);
			}
		}
	}
	
	private static boolean isUnreachable(int y, int x) {
		return y < 0 || y >= N || x < 0 || x >= N;
	}
}