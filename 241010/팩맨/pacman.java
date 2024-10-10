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
		int idx;
		int y;
		int x;
		int dir;
		int destroyTurn;

		public Monster(int idx, int y, int x, int dir) {
			this.idx = idx;
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
	private static List<List<Monster>> map;
	private static Packman packman;
	private static Map<Integer, Monster> monsterMap;
	private static int idx;
	private static Map<Integer, Monster> eggMonsterMap;
	private static Map<Integer, Monster> corpsMonsterMap;
	private static int maxCnt;
	private static List<int[]> maxMovement;

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());

		N = Integer.parseInt(st.nextToken());
		T = Integer.parseInt(st.nextToken());

		st = new StringTokenizer(br.readLine());
		int initY = Integer.parseInt(st.nextToken());
		int initX = Integer.parseInt(st.nextToken());
		packman = new Packman(initY, initX);

		monsterMap = new HashMap<>();
		for (idx = 0; idx < N; idx++) {
			st = new StringTokenizer(br.readLine());

			int y = Integer.parseInt(st.nextToken());
			int x = Integer.parseInt(st.nextToken());
			int dir = Integer.parseInt(st.nextToken());

			monsterMap.put(idx, new Monster(idx, y, x, dir - 1));
		}

		corpsMonsterMap = new HashMap<>();

		int turn = 1;
		while (turn <= T) {
			eggMonsterMap = new HashMap<>();
			monsterClone();
			monsterMove();
			packmanMove(turn);
			monsterCorpsDestroy(turn);
			monsterCloneFinish();

			turn++;
		}

		System.out.println(monsterMap.size());
	}

	public static void monsterClone() {
		for (int element : monsterMap.keySet()) {
			Monster monster = monsterMap.get(element);
			eggMonsterMap.put(idx, new Monster(idx++, monster.y, monster.x, monster.dir));
		}
	}

	public static void monsterMove() {
		for (int element : monsterMap.keySet()) {
			Monster monster = monsterMap.get(element);

			int dir = findMoveableDirection(monster);

			if (dir != -1) {
				monster.dir = dir;
				monster.y = monster.y + dy[monster.dir];
				monster.x = monster.x + dx[monster.dir];
			}
		}

		for (int element : monsterMap.keySet()) {
			Monster monster = monsterMap.get(element);
			//System.out.println("move info : " + element + " " + monster.y + " " + monster.x + " " + monster.dir);
		}

		map = new ArrayList<>();
		for (int i = 0; i <= 4; i++) {
			for (int j = 0; j <= 4; j++) {
				map.add(new ArrayList<>());
			}
		}

		for (int element : monsterMap.keySet()) {
			Monster monster = monsterMap.get(element);
			map.get(monster.y * 5 + monster.x).add(monster);
		}
	}

	private static int findMoveableDirection(Monster monster) {
		int dir = monster.dir;

		for (int i = 0; i < 8; i++) {
			boolean isCorpsExists = false;
			int ny = monster.y + dy[dir];
			int nx = monster.x + dx[dir];

			for (int element : corpsMonsterMap.keySet()) {
				Monster corps = corpsMonsterMap.get(element);
				if (corps.y == ny && corps.x == nx) {
					isCorpsExists = true;
					break;
				}
			}

			if (isCorpsExists || isUnreachable(ny, nx) || (packman.y == ny && packman.x == nx)) {
				dir = (dir + 1) % 8;
				continue;
			}

			return dir;
		}

		return -1;
	}

	public static void packmanMove(int currentTurn) {
		maxCnt = -1;
		maxMovement = new ArrayList<>();

		getMoveablePosition(0, new int[] {packman.y, packman.x}, new ArrayList<>());

		for (int[] row : maxMovement) {
			//System.out.println("move row : " + row[0] + " " + row[1]);
			for (Monster monster : map.get(row[0] * 5 + row[1])) {
				monster.destroyTurn = currentTurn + 2;

				corpsMonsterMap.put(monster.idx, monster);
				monsterMap.remove(monster.idx);
			}

			packman.y = row[0];
			packman.x = row[1];
		}
	}

	private static void getMoveablePosition(int cur, int[] position, List<int[]> positionData) {
		if (cur == 3) {
			int cnt = 0;

			Set<Integer> set = new HashSet<>();
			for (int[] row : positionData) {
				cnt += set.contains(row[0] * 5 + row[1]) ? 0 : map.get(row[0] * 5 + row[1]).size();
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
		List<Integer> destroyKeyList = new ArrayList<>();

		for (int element : corpsMonsterMap.keySet()) {
			if (corpsMonsterMap.get(element).destroyTurn == currentTurn) {
				destroyKeyList.add(element);
			}
		}

		for (int element : destroyKeyList) {
			corpsMonsterMap.remove(element);
		}
	}

	public static void monsterCloneFinish() {
		for (int element : eggMonsterMap.keySet()) {
			Monster monster = eggMonsterMap.get(element);
			monsterMap.put(monster.idx, monster);
		}
	}

	private static boolean isUnreachable(int y, int x) {
		return y < 1 || y > N || x < 1 || x > N;
	}
}