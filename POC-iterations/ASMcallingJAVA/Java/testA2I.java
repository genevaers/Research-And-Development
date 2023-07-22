/* class A2I {

	public native Integer doA2I(String stringin, Integer beginIndex, Integer len);

		Integer showA2I(String stringin, Integer beginIndex, Integer len) {
			System.out.println("stringin: " + stringin);
			String strnum = stringin.substring(beginIndex, beginIndex+len);
			System.out.println("strnum: " + strnum);
			return Integer.MAX_VALUE;	
		}


	    public Integer doAtoi(String stringin, Integer beginIndex, Integer len) {

			String strnum = stringin.substring(beginIndex, beginIndex+len);

			char str[] = strnum.toCharArray();

			int sign = 1, base = 0, i = 0;
		    while (str[i] == ' ')
		    {
			    i++;
		    }
		    if (str[i] == '-' || str[i] == '+')
		    {
			    sign = 1 - 2 * (str[i++] == '-' ? 1 : 0);
		    }
		    while (i < str.length
			    && i <= len
			    && str[i] >= '0'
			    && str[i] <= '9') {
			    if (base > Integer.MAX_VALUE / 10
				    || (base == Integer.MAX_VALUE / 10
					&& str[i] - '0' > 7))
			    {
				    if (sign == 1)
					    return Integer.MAX_VALUE;
				    else
					    return Integer.MIN_VALUE;
			    }
			    base = 10 * base + (str[i++] - '0');
		    }
		    return base * sign;
	    }
}*/

class testA2I {

	public static void main(String[] args) {

		System.out.println("testA2I Started:");
        GVBA2I a = new GVBA2I();
		Integer myint;
		String mystring;
		Integer beginIndex = 16;
		Integer length = 4;
		String mynumber = "ABCDEFGHABCDEFGH0005XYZ";
		String threadIdentifier = "Worker----";

        mystring = String.format("%04d", length);
        System.out.println(mystring);

		mystring = String.format("%6s%04d", threadIdentifier, length);
        System.out.println(mystring);


		myint = a.doAtoi(mynumber, beginIndex, length);
		System.out.println("converted integer: " + myint);

		myint = a.showA2I(mynumber, beginIndex, length);
	}
}
