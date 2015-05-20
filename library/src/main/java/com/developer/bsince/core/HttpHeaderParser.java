
package com.developer.bsince.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

import com.developer.bsince.cache.HttpCache;
import com.developer.bsince.core.assist.Challenge;
import com.developer.bsince.response.NetworkResponse;

/**
 * 解析头的一个实用类
 */
public class HttpHeaderParser {

    //解析响应头，最后封装成一个缓存entry
	public static  HttpCache.Entry parseCacheHeaders(NetworkResponse response) {
        long now = System.currentTimeMillis();

        Headers headers = response.headers;

        long serverDate = 0;
        long lastModified = 0;
        long serverExpires = 0;
        long softExpire = 0;
        long maxAge = 0;
        boolean hasCacheControl = false;

        String serverEtag = null;
        String headerValue;
        headerValue = headers.get("Last-Modified");
        if(headerValue!=null){
        	lastModified = parseDateAsEpoch(headerValue);
        }
        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Cache-Control");
        if (headerValue != null) {
            hasCacheControl = true;
            String[] tokens = headerValue.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (token.equals("no-cache") || token.equals("no-store")) {
                    return null;
                } else if (token.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(token.substring(8));
                    } catch (Exception e) {
                    }
                } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                    maxAge = 0;
                }
            }
        }

        headerValue = headers.get("Expires");
        if (headerValue != null) {
            serverExpires = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        // Cache-Control takes precedence over an Expires header, even if both exist and Expires
        // is more restrictive.
        if (hasCacheControl) {
            softExpire = now + maxAge * 1000;
        } else if (serverDate > 0 && serverExpires >= serverDate) {
            // Default semantic for Expire header in HTTP specification is softExpire.
            softExpire = now + (serverExpires - serverDate);
        }

        HttpCache.Entry entry = new HttpCache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = entry.softTtl;
        entry.serverDate = serverDate;
        entry.lastModifiedTime =lastModified ;
        entry.responseHeaders = headers;

        return entry;
    }

    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    public static long parseDateAsEpoch(String dateStr) {
        try {
            // Parse date in RFC1123 format if this header contains one
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException e) {
            // Date in invalid format, fallback to 0
            return 0;
        }
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    public static String parseCharset(Headers headers) {
        String contentType = headers.get(HTTP.CONTENT_TYPE);
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }

        return HTTP.DEFAULT_CONTENT_CHARSET;
    }

    public static List<Challenge> parseChallenges(Headers responseHeaders, String challengeHeader) {
        // auth-scheme = token
        // auth-param  = token "=" ( token | quoted-string )
        // challenge   = auth-scheme 1*SP 1#auth-param
        // realm       = "realm" "=" realm-value
        // realm-value = quoted-string
        List<Challenge> result = new ArrayList<>();
        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
          if (!challengeHeader.equalsIgnoreCase(responseHeaders.name(i))) {
            continue;
          }
          String value = responseHeaders.value(i);
          int pos = 0;
          while (pos < value.length()) {
            int tokenStart = pos;
            pos = skipUntil(value, pos, " ");

            String scheme = value.substring(tokenStart, pos).trim();
            pos = skipWhitespace(value, pos);

            if (!value.regionMatches(true, pos, "realm=\"", 0, "realm=\"".length())) {
              break; // Unexpected challenge parameter; give up!
            }

            pos += "realm=\"".length();
            int realmStart = pos;
            pos = skipUntil(value, pos, "\"");
            String realm = value.substring(realmStart, pos);
            pos++; // Consume '"' close quote.
            pos = skipUntil(value, pos, ",");
            pos++; // Consume ',' comma.
            pos = skipWhitespace(value, pos);
            result.add(new Challenge(scheme, realm));
          }
        }
        return result;
      }
    
    /**
     * Returns the next index in {@code input} at or after {@code pos} that
     * contains a character from {@code characters}. Returns the input length if
     * none of the requested characters can be found.
     */
    public static int skipUntil(String input, int pos, String characters) {
      for (; pos < input.length(); pos++) {
        if (characters.indexOf(input.charAt(pos)) != -1) {
          break;
        }
      }
      return pos;
    }

    /**
     * Returns the next non-whitespace character in {@code input} that is white
     * space. Result is undefined if input contains newline characters.
     */
    public static int skipWhitespace(String input, int pos) {
      for (; pos < input.length(); pos++) {
        char c = input.charAt(pos);
        if (c != ' ' && c != '\t') {
          break;
        }
      }
      return pos;
    }
}
