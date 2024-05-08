package commerce.emmerce.common;

public class MemberUtil {

    /**
     * 사용자 이름 마스킹 처리
     * @param existsName
     * @return
     */
    public static String maskingMemberName(String existsName) {
        StringBuilder sb = new StringBuilder();
        sb.append(existsName.substring(0,1));
        sb.append("********");
        sb.append(existsName.substring(existsName.length() - 1));

        return sb.toString();
    }

}
