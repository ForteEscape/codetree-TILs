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

		int Q = Integer.parseInt(br.readLine());

		for (int i = 0; i < Q; i++) {
			st = new StringTokenizer(br.readLine());
			int cmd = Integer.parseInt(st.nextToken());

			if (cmd == 100) {
				int N = Integer.parseInt(st.nextToken());
				String url = st.nextToken();

				init(N, url);
			} else if (cmd == 200) {
				int t = Integer.parseInt(st.nextToken());
				int p = Integer.parseInt(st.nextToken());
				String url = st.nextToken();

				requestJudge(t, p, url);
			} else if (cmd == 300) {
				int t = Integer.parseInt(st.nextToken());

				judge(t);
			} else if (cmd == 400) {
				int t = Integer.parseInt(st.nextToken());
				int jid = Integer.parseInt(st.nextToken());

				exitJudge(t, jid);
			} else if (cmd == 500) {
				int t = Integer.parseInt(st.nextToken());

				sb.append(getJudgeTask(t)).append("\n");
			}
		}

		System.out.print(sb);
	}

	/**
	 * 초기화 메서드
	 * @param N 채점기의 수
	 * @param url 최초 채점 요청 문제의 url
	 */
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

	/**
	 * 채점 요청 메서드 <br>
	 *
	 * @param t 현재 시간
	 * @param p 요청한 문제의 우선순위
	 * @param url 요청한 문제의 url
	 */
	public static void requestJudge(int t, int p, String url) {
		// url 이 완벽하게 일치하는 문제가 이미 대기 큐에 존재하는 경우 무시된다.
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
	 * 채점을 수행하는 메서드
	 * @param t 채점 시각
	 */
	public static void judge(int t) {
		if (judgeDevicePriorityQueue.isEmpty()) {
			return;
		}
		waitingQueue = new PriorityQueue<>();

		// 채점 가능한 데이터들을 waiting queue 에 넣는다.
		getJudgeableProblems(t);
		if (waitingQueue.isEmpty()) {
			return;
		}

		// 해당 채점 가능한 데이터들 중 가장 우선순위가 큰 데이터를 넣는다.
		Problem problem = waitingQueue.poll();
		problem.startTime = t;

		// 채점 중인 채점기로 상태 변경
		int graderId = judgeDevicePriorityQueue.poll();
		judgingDataMap.put(graderId, problem);

		// 해당 문제의 도메인을 채점 중인 도메인 set 에 추가
		String domain = getDomain(problem.url);
		judgingProblemDomainSet.add(domain);

		// 채점 중인 문제 데이터를 대기 큐에서 제거
		domainMap.get(domain).pollFirst();
		waitingQueueProblemUrlSet.remove(problem.url);
	}

	/**
	 * {@code Map<String, TreeSet<Problem>>}의 자료구조를 하나 만든다. <br>
	 * key 는 도메인 스트링 <br><br>
	 * 채점 시 다음과 같은 로직을 구성한다. <br><br>
	 * 1. Map 의 모든 key(도메인)을 가져온다. <br>
	 * 2. 이 도메인 중 이미 채점이 진행중인 도메인들을 건너띈다. <br>
	 * 3. 남은 도메인들에 대해 다음의 검증을 진행한다. <br>
	 * 	 3-1. 해당 도메인에 대한 {@code limitTime} 을 계산한다. 이때 {@code limitTime} 은 위의 {@code start + 3 * gap} 이다. <br>
	 * 	 3-2. 만약 현재 시간 t가 limit time 보다 작다면 해당 도메인은 건너띈다. <br>
	 * 4. 남은 데이터들 중 가장 우선순위가 높은 데이터를 Priority Queue 에 넣는다. <br>
	 *
	 * @param t 요청된 시간
	 */
	private static void getJudgeableProblems(int t) {
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

			// 4. 남은 데이터들을 Priority Queue 에 넣는다.
			if (!domainMap.get(domain).isEmpty()) {
				waitingQueue.add(domainMap.get(domain).first());
			}
		}
	}

	/**
	 * 채점 종료 요청 메서드
	 * @param t 요청 시간 
	 * @param jid 채점을 종료시킬 채점기 id
	 */
	public static void exitJudge(int t, int jid) {
		if (!judgingDataMap.containsKey(jid)) {
			return;
		}

		Problem judgingProblem = judgingDataMap.get(jid);

		st = new StringTokenizer(judgingProblem.url, "/");
		String domain = st.nextToken();

		judgingProblem.endTime = t;

		// 채점이 끝났으므로 해당 문제를 채점 기록에 저장한다.
		problemHistoryMap.put(domain, judgingProblem);

		// 채점 중인 도메인에서 해당 도메인을 제거한다.
		judgingProblemDomainSet.remove(domain);
		// 채점 중인 채점기를 다시 대기 중으로 상태를 변경한다.
		judgeDevicePriorityQueue.add(jid);
		// 채점기가 채점 중인 문제 map 에서 제거한다.
		judgingDataMap.remove(jid);
	}

	/**
	 * 현재 대기 큐에 대기중인 모든 문제의 개수를 반환한다.
	 * @param t 요청 시각 t
	 * @return 대기 큐에 대기중인 모든 문제의 개수
	 */
	public static int getJudgeTask(int t) {
		int res = 0;

		// 대기 큐에 존재하는 문제 데이터들을 domain 별로 찾는다.
		for (String domain : domainMap.keySet()) {
			res += domainMap.get(domain).size(); // 해당 domain 에 소속된 문제들의 개수들을 모두 더한다.
		}

		return res;
	}

	private static String getDomain(String url) {
		st = new StringTokenizer(url, "/");

		return st.nextToken();
	}

}