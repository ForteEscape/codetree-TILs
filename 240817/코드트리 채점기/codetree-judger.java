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
			return "Problem [time=" + time + ", priority=" + priority + ", url=" + url + "]";
		}
	}

	// 대기중인 채점기의 id priority queue
	private static PriorityQueue<Integer> judgeDevicePriorityQueue;
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

		System.out.println(sb);
	}

	public static void init(int N, String url) {
		judgeDevicePriorityQueue = new PriorityQueue<>();
		problemHistoryMap = new HashMap<>();
		judgingProblemDomainSet = new HashSet<>();
		waitingQueueProblemUrlSet = new HashSet<>();
		waitingQueue = new PriorityQueue<>();
		judgingDataMap = new HashMap<>();

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
		waitingQueue.add(problem);
		waitingQueueProblemUrlSet.add(url);
	}

	public static void judge(int t) {
		if (judgeDevicePriorityQueue.isEmpty()) {
			return;
		}

		List<Problem> cannotJudgeProblemList = new ArrayList<>();

		while (!waitingQueue.isEmpty()) {
			Problem problem = waitingQueue.poll();

			st = new StringTokenizer(problem.url, "/");
			String domain = st.nextToken();

			if (judgingProblemDomainSet.contains(domain)) {
				cannotJudgeProblemList.add(problem);
				continue;
			}

			if (problemHistoryMap.containsKey(domain)) {
				Problem judgedProblem = problemHistoryMap.get(domain);

				int gap = judgedProblem.endTime - judgedProblem.startTime;
				int limitTime = judgedProblem.startTime + 3 * gap;

				if (limitTime > t) {
					cannotJudgeProblemList.add(problem);
					continue;
				}
			}

			int judgerId = judgeDevicePriorityQueue.poll();

			problem.startTime = t;
			judgingProblemDomainSet.add(domain);
			judgingDataMap.put(judgerId, problem);
			waitingQueueProblemUrlSet.remove(problem.url);

			break;
		}

		waitingQueue.addAll(cannotJudgeProblemList);
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
		return waitingQueue.size();
	}

}