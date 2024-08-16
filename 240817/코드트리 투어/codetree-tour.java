import java.util.*;
import java.io.*;

public class Main {

	static class Edge {
		int dest;
		int weight;

		public Edge(int dest, int weight) {
			this.dest = dest;
			this.weight = weight;
		}
	}

	static class Product {
		int id;
		int revenue;
		int dest;

		public Product(int id, int revenue, int dest) {
			this.id = id;
			this.revenue = revenue;
			this.dest = dest;
		}
	}

	static class Benefit {
		int id;
		int benefit;

		public Benefit(int id, int benefit) {
			this.id = id;
			this.benefit = benefit;
		}
	}

	private static List<List<Edge>> graph;
	private static int startingPoint = 0;
	private static final int MAX_CAPACITY = 30_001;
	private static Product[] products;
	private static Benefit[] benefits;
	private static Set<Integer> availableProducts;
	private static int[] cost;
	private static int N;
	private static Set<Integer> notCalculatedSet;
	private static TreeSet<Benefit> benefitTreeSet;

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		int Q = Integer.parseInt(br.readLine());

		StringTokenizer st;
		for (int i = 0; i < Q; i++) {
			st = new StringTokenizer(br.readLine());

			int cmd = Integer.parseInt(st.nextToken());

			if (cmd == 100) {
				// init
				int N = Integer.parseInt(st.nextToken());
				int M = Integer.parseInt(st.nextToken());

				List<Integer> infoList = new ArrayList<>();
				while (st.hasMoreTokens()) {
					infoList.add(Integer.parseInt(st.nextToken()));
				}

				init(N, M, infoList);
			} else if (cmd == 200) {
				// create
				int id = Integer.parseInt(st.nextToken());
				int revenue = Integer.parseInt(st.nextToken());
				int dest = Integer.parseInt(st.nextToken());

				create(id, revenue, dest);
			} else if (cmd == 300) {
				// cancel
				int id = Integer.parseInt(st.nextToken());

				cancel(id);
			} else if (cmd == 400) {
				// selling
				System.out.println(selling());
			} else {
				// change starting point
				int nextStartingPoint = Integer.parseInt(st.nextToken());

				changeStartingPoint(nextStartingPoint);
			}
		}
	}

	private static void init(int n, int m, List<Integer> infoList) {
		graph = new ArrayList<>();

		N = n;

		for (int i = 0; i < n; i++) {
			graph.add(new ArrayList<>());
		}

		for (int i = 0; i < m * 3; i += 3) {
			int src = infoList.get(i);
			int dest = infoList.get(i + 1);
			int weight = infoList.get(i + 2);

			graph.get(src).add(new Edge(dest, weight));
			graph.get(dest).add(new Edge(src, weight));
		}

		products = new Product[MAX_CAPACITY];
		benefits = new Benefit[MAX_CAPACITY];

		availableProducts = new HashSet<>();
		notCalculatedSet = new HashSet<>();
		benefitTreeSet = new TreeSet<>(
			(o1, o2) -> o2.benefit - o1.benefit == 0 ? o1.id - o2.id : o2.benefit - o1.benefit);

		// dijkstra from starting point 0
		dijkstra();
	}

	private static void create(int id, int revenue, int dest) {
		products[id] = new Product(id, revenue, dest);
		availableProducts.add(id);

		notCalculatedSet.add(id);
	}

	private static void cancel(int id) {
		if (availableProducts.contains(id)) {
			products[id] = null;
			availableProducts.remove(id);
		}

		notCalculatedSet.remove(id);

		if(benefits[id] != null) {
			benefitTreeSet.remove(benefits[id]);
			benefits[id] = null;
		}
	}

	private static int selling() {
		// shortest path 로 구해진 데이터를 기반으로 이득 계산
		// 이미 계산된 Product 이면 다시 계산할 필요가 없다. => starting point 가 변경되면 다시 계산해야한다.
		calcBenefit();
		// 최대 이익 상품 제거
		Benefit result = benefitTreeSet.pollFirst();

		if (result == null) {
			return -1;
		}

		cancel(result.id);

		return result.id;
	}

	private static void changeStartingPoint(int nextStartingPoint) {
		startingPoint = nextStartingPoint;
		dijkstra();

		// 여기에서 이미 관리 중인 데이터들을 다시 재계산을 수행해야 한다.
		notCalculatedSet = new HashSet<>(availableProducts);
		benefitTreeSet.clear();
		calcBenefit();
	}

	private static void calcBenefit() {
		for (int id : notCalculatedSet) {
			// 도달 불능점인 경우 스킵
			if (cost[products[id].dest] == Integer.MAX_VALUE) {
				continue;
			}

			int benefit = products[id].revenue - cost[products[id].dest];
			if (benefit < 0) {
				continue;
			}

			Benefit benefitData = new Benefit(id, benefit);
			benefits[id] = benefitData;
			benefitTreeSet.add(benefitData);
		}

		notCalculatedSet.clear();
	}

	private static void dijkstra() {
		cost = new int[N];
		for (int i = 0; i < N; i++) {
			cost[i] = Integer.MAX_VALUE;
		}
		cost[startingPoint] = 0;

		PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));
		pq.add(new Edge(startingPoint, 0));

		while (!pq.isEmpty()) {
			Edge edge = pq.poll();

			if (edge.weight > cost[edge.dest]) {
				continue;
			}

			for (Edge adjNode : graph.get(edge.dest)) {
				if (cost[adjNode.dest] > cost[edge.dest] + adjNode.weight) {
					cost[adjNode.dest] = cost[edge.dest] + adjNode.weight;
					pq.offer(new Edge(adjNode.dest, cost[adjNode.dest]));
				}
			}
		}
	}
}