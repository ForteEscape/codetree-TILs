import java.util.*;
import java.io.*;

public class Main {

    static int[][] board1;
    static int[][] board2;

    public static void main(String[] args) throws Exception {
        // 여기에 코드를 작성해주세요.
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        board1 = new int[4][2];
        board2 = new int[4][2];

        for(int i = 0; i < 4; i++) {
            st = new StringTokenizer(br.readLine());

            int a = Integer.parseInt(st.nextToken());
            int b = Integer.parseInt(st.nextToken());

            board1[i] = new int[]{a, b};
        }

        br.readLine();

        for(int i = 0; i < 4; i++) {
            st = new StringTokenizer(br.readLine());

            int a = Integer.parseInt(st.nextToken());
            int b = Integer.parseInt(st.nextToken());

            board2[i] = new int[]{a, b};
        }

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 4; i++) {
            sb.append(board1[i][0] * board2[i][0]).append(" ").append(board1[i][1] * board2[i][1]).append("\n");
        }
        System.out.print(sb);
    }
}