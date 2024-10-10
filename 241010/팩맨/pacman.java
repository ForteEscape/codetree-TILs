import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class Main {

	public static class Monster {
		int y;
		int x;
		int dir;
		int destroyTurn;

		public Monster(int y, int x, int dir) {
			this.y = y;
			this.x = x;
			this.dir = dir;
			this.destroyTurn = -1;
		}
	}

	public static class Packman {
		int y;
		int x;

		public Packman(int y, int x) {
			this.y = y;
			this.x = x;
		}
	}

	private static int N, T;
	private static int[] dy = {-1, -1, 0, 1, 1, 1, 0, -1};
	private static int[] dx = {0, -1, -1, -1, 0, 1, 1, 1};
	private static int[] packmanDy = {-1, 0, 1, 0};
	private static int[] packmanDx = {0, -1, 0, 1};
	private static Packman packman;
	private static int idx;
	private static int maxCnt;
	private static List<int[]> maxMovement;
	private static Set<Monster>[][] map;
	private static Set<Monster>[][] corps;
	private static Map<Integer, List<Monster>> corpsMap;
	private static Set<Monster> aliveMonster;
	private static Set<Monster> eggMonster;

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());

		N = Integer.parseInt(st.nextToken());
		T = Integer.parseInt(st.nextToken());

		st = new StringTokenizer(br.readLine());
		int initY = Integer.parseInt(st.nextToken());
		int initX = Integer.parseInt(st.nextToken());
		packman = new Packman(initY, initX);

		map = new Set[5][5];
		corps = new Set[5][5];
		corpsMap = new HashMap<>();
		eggMonster = new HashSet<>();
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				corps[i][j] = new HashSet<>();
				map[i][j] = new HashSet<>();
			}
		}

		aliveMonster = new HashSet<>();
		for (idx = 0; idx < N; idx++) {
			st = new StringTokenizer(br.readLine());

			int y = Integer.parseInt(st.nextToken());
			int x = Integer.parseInt(st.nextToken());
			int dir = Integer.parseInt(st.nextToken());

			Monster monster = new Monster(y, x, dir - 1);
			map[y][x].add(monster);
			aliveMonster.add(monster);
		}

		int turn = 1;
		while (turn <= T) {
			monsterClone();
			monsterMove();
			packmanMove(turn);
			monsterCorpsDestroy(turn);
			monsterCloneFinish();

			turn++;
		}

		System.out.println(aliveMonster.size());
	}

	public static void monsterClone() {
		for (Monster monster : aliveMonster) {
			eggMonster.add(new Monster(monster.y, monster.x, monster.dir));
		}
	}

	public static void monsterMove() {
		for (Monster monster : aliveMonster) {
			int dir = findMoveableDirection(monster);

			if (dir != -1) {
				monster.dir = dir;
				monster.y = monster.y + dy[monster.dir];
				monster.x = monster.x + dx[monster.dir];
			}
		}
	}

	private static int findMoveableDirection(Monster monster) {
		int dir = monster.dir;

		for (int i = 0; i < 8; i++) {
			int ny = monster.y + dy[dir];
			int nx = monster.x + dx[dir];

			if (isUnreachable(ny, nx) || !corps[ny][nx].isEmpty() || (packman.y == ny && packman.x == nx)) {
				dir = (dir + 1) % 8;
				continue;
			}

			map[monster.y][monster.x].remove(monster);
			map[ny][nx].add(monster);

			return dir;
		}

		return -1;
	}

	public static void packmanMove(int currentTurn) {
		maxCnt = -1;
		maxMovement = new ArrayList<>();

		getMoveablePosition(0, new int[] {packman.y, packman.x}, new ArrayList<>());
		if (!corpsMap.containsKey(currentTurn + 2)) {
			corpsMap.put(currentTurn + 2, new ArrayList<>());
		}

		for (int[] row : maxMovement) {
			for (Monster monster : map[row[0]][row[1]]) {
				corps[monster.y][monster.x].add(monster);
				corpsMap.get(currentTurn + 2).add(monster);
				aliveMonster.remove(monster);
			}
			map[row[0]][row[1]] = new HashSet<>();
			packman.y = row[0];
			packman.x = row[1];
		}
	}

	private static void getMoveablePosition(int cur, int[] position, List<int[]> positionData) {
		if (cur == 3) {
			int cnt = 0;

			Set<Integer> set = new HashSet<>();
			for (int[] row : positionData) {
				cnt += set.contains(row[0] * 5 + row[1]) ? 0 : map[row[0]][row[1]].size();
				set.add(row[0] * 5 + row[1]);
			}

			if (cnt > maxCnt) {
				maxMovement = new ArrayList<>();
				maxCnt = cnt;
				for (int[] row : positionData) {
					maxMovement.add(new int[] {row[0], row[1]});
				}
			}

			return;
		}

		for (int i = 0; i < 4; i++) {
			int ny = position[0] + packmanDy[i];
			int nx = position[1] + packmanDx[i];

			if (isUnreachable(ny, nx)) {
				continue;
			}

			positionData.add(new int[] {ny, nx});
			getMoveablePosition(cur + 1, new int[] {ny, nx}, positionData);
			positionData.remove(positionData.size() - 1);
		}
	}

	public static void monsterCorpsDestroy(int currentTurn) {
		if (!corpsMap.containsKey(currentTurn)) {
			return;
		}

		List<Monster> corpsList = corpsMap.get(currentTurn);
		for (Monster monster : corpsList) {
			corps[monster.y][monster.x].remove(monster);
		}
		corpsMap.remove(currentTurn);
	}

	public static void monsterCloneFinish() {
		for (Monster monster : eggMonster) {
			map[monster.y][monster.x].add(monster);
			aliveMonster.add(monster);
		}
		eggMonster = new HashSet<>();
	}

	private static boolean isUnreachable(int y, int x) {
		return y < 1 || y > 4 || x < 1 || x > 4;
	}
}