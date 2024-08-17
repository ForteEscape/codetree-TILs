import java.util.*;
import java.io.*;

public class Main {

	public static class Problem implements Comparable<Problem> {
		int time;
		int priority;
		int endTime;
		int startTime;
		String url;

		public Problem(int time, int priority, String url) {
			this.time = time;
			this.priority = priority;
			this.url = url;
		}

		@Override
		public int compareTo(Problem o) {
			int result = Integer.compare(this.priority, o.priority);

			if (result == 0) {
				return Integer.compare(this.time, o.time);
			}
			return result;
		}

		@Override
		public String toString() {
			return "[time " + time + " priority " + priority + " endTime " + endTime + " startTime " + startTime
				+ " url " + url + "]";
		}
	}

	// 대기중인 채점기의 id priority queue
	private static PriorityQueue<Integer> judgeDevicePriorityQueue;
	// 해당 도메인에 소속된 문제들을 저장하는 Map
	private static Map<String, TreeSet<Problem>> domainMap;
	// 채점기의 상태(채점기 id, 채점기가 채점 중인 Problem)
	private static Map<Integer, Problem> judgingDataMap;
	// 가장 최근에 채점한 도메인의 데이터 맵
	private static Map<String, Problem> problemHistoryMap;
	// 채점 중인 도메인 set
	private static Set<String> judgingProblemDomainSet;
	// 채점 큐에 대기 중인 도메인 set
	private static Set<String> waitingQueueProblemUrlSet;
	// 채점 대기 큐
	private static PriorityQueue<Problem> waitingQueue;
	private static StringTokenizer st;

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringBuilder sb = new StringBuilder();
		StringTokenizer cst;

		int Q = Integer.parseInt(br.readLine());

		for (int i = 0; i < Q; i++) {
			cst = new StringTokenizer(br.readLine());
			int cmd = Integer.parseInt(cst.nextToken());

			if (cmd == 100) {
				int N = Integer.parseInt(cst.nextToken());
				String url = cst.nextToken();

				init(N, url);
			} else if (cmd == 200) {
				int t = Integer.parseInt(cst.nextToken());
				int p = Integer.parseInt(cst.nextToken());
				String url = cst.nextToken();

				requestJudge(t, p, url);
			} else if (cmd == 300) {
				int t = Integer.parseInt(cst.nextToken());

				judge(t);
			} else if (cmd == 400) {
				int t = Integer.parseInt(cst.nextToken());
				int jid = Integer.parseInt(cst.nextToken());

				exitJudge(t, jid);
			} else if (cmd == 500) {
				int t = Integer.parseInt(cst.nextToken());

				sb.append(getJudgeTask(t)).append("\n");
			}
		}

		System.out.print(sb);
	}

	public static void init(int N, String url) {
		judgeDevicePriorityQueue = new PriorityQueue<>();
		problemHistoryMap = new HashMap<>();
		judgingProblemDomainSet = new HashSet<>();
		waitingQueueProblemUrlSet = new HashSet<>();
		waitingQueue = new PriorityQueue<>();
		judgingDataMap = new HashMap<>();
		domainMap = new HashMap<>();

		for (int i = 1; i <= N; i++) {
			judgeDevicePriorityQueue.add(i);
		}

		requestJudge(0, 1, url);
	}

	public static void requestJudge(int t, int p, String url) {
		if (waitingQueueProblemUrlSet.contains(url)) {
			return;
		}

		Problem problem = new Problem(t, p, url);
		String domain = getDomain(url);

		if (!domainMap.containsKey(domain)) {
			domainMap.put(domain, new TreeSet<>());
		}
		domainMap.get(domain).add(problem);
		waitingQueueProblemUrlSet.add(url);
	}

	/**
	 * Map<String, Set<Problem>>의 자료구조를 하나 만든다. <br>
	 * String 은 도메인, TreeMap 에서 Integer 의 경우 해당 Problem 이 waiting queue 에 들어온 시간 t <br><br>
	 * 채점 시 다음과 같은 로직을 구성한다. <br><br>
	 * 1. Map 의 모든 key(도메인)을 가져온다. <br>
	 * 2. 이 도메인 중 이미 채점이 진행중인 도메인들을 건너띈다. <br>
	 * 3. 남은 도메인들에 대해 다음의 검증을 진행한다. <br>
	 * 	 3-1. 해당 도메인에 대한 limit time 을 계산한다. 이때 limit time 은 위의 start + 3 * gap 이다. <br>
	 * 	 3-2. 만약 현재 시간 t가 limit time 보다 작다면 해당 도메인은 건너띈다. <br>
	 * 4. 남은 데이터들을 Priority Queue 에 밀어넣고 적합한 데이터를 가져온다. <br>
	 *
	 * @param t 요청된 시간
	 */
	public static void judge(int t) {
		if (judgeDevicePriorityQueue.isEmpty()) {
			return;
		}
		waitingQueue.clear();

		for (String domain : domainMap.keySet()) {
			// 도메인 중 이미 채점이 진행중인 도메인들을 건너띈다.
			if (judgingProblemDomainSet.contains(domain)) {
				continue;
			}

			if (problemHistoryMap.containsKey(domain)) {
				Problem judgedHistoryProblem = problemHistoryMap.get(domain);

				// 해당 도메인에 대한 limit time 을 계산한다. 이때 limit time 은 위의 start + 3 * gap 이다.
				int gap = judgedHistoryProblem.endTime - judgedHistoryProblem.startTime;
				int limitTime = judgedHistoryProblem.startTime + 3 * gap;

				// 만약 현재 시간 t가 limit time 보다 작다면 해당 도메인은 건너띈다.
				if (t < limitTime) {
					continue;
				}
			}

			// 4. 남은 데이터들을 Priority Queue 에 밀어넣는다.
			if (!domainMap.get(domain).isEmpty()) {
				waitingQueue.add(domainMap.get(domain).first());
			}
		}

		if (waitingQueue.isEmpty()) {
			return;
		}

		Problem problem = waitingQueue.poll();
		int graderId = judgeDevicePriorityQueue.poll();
		judgingDataMap.put(graderId, problem);

		problem.startTime = t;
		String domain = getDomain(problem.url);
		judgingProblemDomainSet.add(domain);

		domainMap.get(domain).pollFirst();
		waitingQueueProblemUrlSet.remove(problem.url);
	}

	public static void exitJudge(int t, int jid) {
		if (!judgingDataMap.containsKey(jid)) {
			return;
		}

		Problem judgingProblem = judgingDataMap.get(jid);

		st = new StringTokenizer(judgingProblem.url, "/");
		String domain = st.nextToken();

		judgingProblem.endTime = t;

		problemHistoryMap.put(domain, judgingProblem);
		judgingProblemDomainSet.remove(domain);

		judgeDevicePriorityQueue.add(jid);

		judgingDataMap.remove(jid);
	}

	public static int getJudgeTask(int t) {
		int res = 0;

		for (String domain : domainMap.keySet()) {
			res += domainMap.get(domain).size();
		}

		return res;
	}

	private static String getDomain(String url) {
		st = new StringTokenizer(url, "/");

		return st.nextToken();
	}

}