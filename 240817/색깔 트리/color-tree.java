import java.util.*;
import java.io.*;

public class Main {

	public static class Node {
		int mid;
		int maxDepth;
		int color;
		int version;
		int currentDepth;

		int parent;
		Set<Integer> childSet;

		public Node(int mid, int color, int maxDepth) {
			this.mid = mid;
			this.color = color;
			this.maxDepth = maxDepth;

			childSet = new HashSet<>();
		}
	}

	private static final int MAX_CAPACITY = 100_001;
	private static int version = 0;
	private static Node[] nodes;
	private static List<Integer> rootNodeMidList;

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st;
		StringBuilder sb = new StringBuilder();

		int Q = Integer.parseInt(br.readLine());

		init();

		for (int i = 0; i < Q; i++) {
			st = new StringTokenizer(br.readLine());

			int cmd = Integer.parseInt(st.nextToken());
			if (cmd == 100) {
				int mid = Integer.parseInt(st.nextToken());
				int pid = Integer.parseInt(st.nextToken());
				int color = Integer.parseInt(st.nextToken());
				int maxDepth = Integer.parseInt(st.nextToken());

				addNode(mid, pid, color, maxDepth);
			} else if (cmd == 200) {
				int mid = Integer.parseInt(st.nextToken());
				int color = Integer.parseInt(st.nextToken());

				changeColor(mid, color);
			} else if (cmd == 300) {
				int mid = Integer.parseInt(st.nextToken());
				
				sb.append(getColor(mid)).append("\n");
			} else {
				sb.append(getPrice()).append("\n");
			}
		}

		System.out.print(sb);
	}

	private static void init() {
		nodes = new Node[MAX_CAPACITY];
		rootNodeMidList = new ArrayList<>();
	}

	/**
	 * 노드를 트리에 추가한다.
	 * @param mid 고유 id
	 * @param pid 부모 노드 id
	 * @param color 노드의 현재 색
	 * @param maxDepth 최대 깊이
	 */
	public static void addNode(int mid, int pid, int color, int maxDepth) {
		if (pid == -1) {
			rootNodeMidList.add(mid);

			Node newNode = add(mid, pid, color, maxDepth);
			newNode.currentDepth = 1;

			nodes[mid] = newNode;
			return;
		}

		if (isValid(pid)) {
			Node newNode = add(mid, pid, color, maxDepth);
			newNode.currentDepth = nodes[pid].currentDepth + 1;

			nodes[mid] = newNode;
			nodes[pid].childSet.add(mid);
		}
	}

	/**
	 * 노드를 생성한다.
	 *
	 * @param mid 고유 id
	 * @param pid 부모 노드 id
	 * @param color 현재 색
	 * @param maxDepth 최대 깊이
	 * @return 생성된 새로운 노드
	 */
	private static Node add(int mid, int pid, int color, int maxDepth) {
		Node node = new Node(mid, color, maxDepth);
		nodes[mid] = node;

		node.version = version++;
		node.parent = pid;

		return node;
	}

	/**
	 * 최대 깊이에 모순되는지 검증한다. <br>
	 * 처음 노드는 반드시 루트 노드가 된다. 이때 이 노드의 현재 깊이를 1로 정의한다.
	 * 이후 새로운 노드가 들어와 검증될 때 그 노드의 깊이는 부모 노드의 현재 깊이 + 1 이다. <br>
	 * 이때 이 현재 깊이 - 부모 노드의 현재 깊이의 값이 부모 노드의 최대 깊이값 보다 크거나 같은지 검사한다.
	 *
	 * @param pid 부모 노드의 id
	 * @return 모순점이 존재하는지에 대한 결과
	 */
	private static boolean isValid(int pid) {
		int currentNodeDepth = nodes[pid].currentDepth + 1;

		while (pid != -1) {
			if (currentNodeDepth - nodes[pid].currentDepth >= nodes[pid].maxDepth) {
				return false;
			}

			pid = nodes[pid].parent;
		}

		return true;
	}

	/**
	 * 특정 노드 mid 를 루트로 하는 서브트리의 모든 노드의 색을 지정된 색 color 로 변경한다.
	 * O(1)
	 * @param mid 대상 노드 id
	 * @param color 변경할 색
	 */
	public static void changeColor(int mid, int color) {
		Node node = nodes[mid];

		node.color = color;
		node.version = version++;
	}

	/**
	 * 특정 노드 mid 의 현재 색을 조회한다. O(N)
	 * @param mid 노드 id
	 * @return mid 노드의 현재 색
	 */
	public static int getColor(int mid) {
		int currentNodeColor = nodes[mid].color;
		int currentNodeVersion = nodes[mid].version;

		// 현재 버전과 동일하다면 이미 최신이라는 것과 동치이므로 해당 색을 그대로 반환한다.
		if (currentNodeVersion == version) {
			return currentNodeColor;
		}

		int pid = nodes[mid].parent;

		while (pid != -1) {
			Node parentNode = nodes[pid];

			if (parentNode.version > currentNodeVersion) {
				currentNodeColor = parentNode.color;
				currentNodeVersion = parentNode.version;
			}

			pid = parentNode.parent;
		}

		return currentNodeColor;
	}

	/**
	 * 모든 노드의 가치를 제곱한 결과를 반환한다.
	 *
	 * @return 모든 노드의 가치 제곱 값
	 */
	public static long getPrice() {
		long result = 0L;

		for (int rootNodeMid : rootNodeMidList) {
			result += calcPrice(rootNodeMid, new HashSet<>());
		}

		return result;
	}

	/**
	 * 가치 총합의 제곱을 반환하는 함수
	 *
	 * @param mid 현재 노드
	 * @param colorSet 내 자식 노드들의 색 집합
	 * @return 현재까지 탐사한 가치 총합의 제곱 값
	 */
	private static long calcPrice(int mid, Set<Integer> colorSet) {
		Node currentNode = nodes[mid];

		long result = 0L;

		// 이 노드를 루트로 하는 서브트리에서 나올 수 있는 모든 색들의 집합
		Set<Integer> currentColorSet = new HashSet<>();
		for (int childNode : currentNode.childSet) {
			result += calcPrice(childNode, colorSet);

			// 해당 노드를 루트로 하는 서브트리의 한 분기에서 나온 모든 색들을 넣는다.
			currentColorSet.addAll(colorSet);

			// 이후 해당 서브트리의 한 분기에서 나온 색들을 정리한다.
			colorSet.clear();
		}

		int currentNodeColor = getColor(mid);
		long price = currentColorSet.size();

		if (!currentColorSet.contains(currentNodeColor)) {
			price++;
		}

		colorSet.add(currentNodeColor);
		colorSet.addAll(currentColorSet);

		return result + price * price;
	}

}