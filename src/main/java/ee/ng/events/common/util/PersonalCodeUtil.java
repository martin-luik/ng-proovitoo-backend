package ee.ng.events.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PersonalCodeUtil {

    public static final String EE_ID_CODE_PREFIX = "EE";

    public static boolean isValidPersonalCode(String idCode) {
        if (idCode.startsWith(EE_ID_CODE_PREFIX)) {
            idCode = idCode.substring(2);
        }

        if (idCode.length() != 11) {
            return false;
        }

        int[] digits = idCode.chars().map(Character::getNumericValue).toArray();
        int[] valueDigits = Arrays.copyOfRange(digits, 0, 10);
        int checksumDigit = digits[10];

        return checksumDigit == checksum(valueDigits);
    }

    private static int weightedChecksum(int[] digits, int[] weights) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += digits[i] * weights[i];
        }

        return sum % 11;
    }

    public static boolean isNotValidPersonalCode(String idCode) {
        return !isValidPersonalCode(idCode);
    }

    private static int checksum(int[] digits) {
        int checksum1 = weightedChecksum(digits, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 1});
        if (checksum1 < 10) {
            return checksum1;
        }

        int checksum2 = weightedChecksum(digits, new int[]{3, 4, 5, 6, 7, 8, 9, 1, 2, 3});
        if (checksum2 < 10) {
            return checksum2;
        }

        return 0;
    }

    public static String removeEEPrefixIfPresent(String idCode) {
        if (idCode == null) {
            return null;
        }

        if (idCode.startsWith(EE_ID_CODE_PREFIX)) {
            return idCode.substring(2);
        }
        return idCode;
    }

    public static String addEEPrefixIfNotPresent(String idCode) {
        if (idCode == null) {
            return null;
        }

        if (!idCode.contains(EE_ID_CODE_PREFIX)) {
            return EE_ID_CODE_PREFIX + idCode;
        }
        return idCode;
    }

    public static List<String> addEEPrefixIfNotPresent(List<String> idCodes) {
        if (idCodes.isEmpty()) {
            return idCodes;
        }

        return idCodes.stream()
                .map(PersonalCodeUtil::addEEPrefixIfNotPresent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
