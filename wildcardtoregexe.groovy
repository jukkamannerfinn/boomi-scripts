import java.util.regex.Pattern;
public static String wildcardToRegex(String wildcard){
    StringBuffer s = new StringBuffer(wildcard.length());
    s.append('^');
    int i = 0;
    int is = 0;
    for (is = wildcard.length(); i < is; i++) {
        char c = wildcard.charAt(i);
        switch(c) {
            case '*':
                s.append(".*");
                break;
            case '?':
                s.append(".");
                break;
                // escape special regexp-characters
            case '(': case ')': case '[': case ']': case '$':
            case '^': case '.': case '{': case '}': case '|':
            case '\\':
                s.append("\\");
                s.append(c);
                break;
            default:
                s.append(c);
                break;
        }
    }
//    s.append('$');
    return(s.toString());
}
filepath = "outbound/pba/partner_billing_member_transactions/yyyy=2022/mm=03/dd=02";

wildcardToRegex(filepath + "/")