package com.mattibal.meshnet.utils.color;

public class HuslConverter {

	//Pass in HUSL values and get back RGB values, H ranges from 0 to 360, S and L from 0 to 100.
	//RGB values will range from 0 to 1.
	public static double[] HUSLtoRGB( double h, double s,double l )
	{
		return XYZ_RGB(LUV_XYZ(LCH_LUV(HUSL_LCH(new double[] {h, s, l}))));
	}

	//Pass in RGB values ranging from 0 to 1 and get back HUSL values.
	//H ranges from 0 to 360, S and L from 0 to 100.
	public static double[] RGBtoHUSL( double r, double g, double b )
	{
		return LCH_HUSL(LUV_LCH(XYZ_LUV(RGB_XYZ(new double[] {r, g, b}))));
	}	

	
	public static double[] HUSLtoXYZ(double h, double s, double l){
		return LUV_XYZ(LCH_LUV(HUSL_LCH(new double[] {h, s, l})));
	}
	
	
	private static double PI = 3.1415926535897932384626433832795;
	private static double m[][] = {{3.2406f, -1.5372f, -0.4986f}, 
								  {-0.9689f, 1.8758f, 0.0415f}, 
								  {0.0557f, -0.2040f, 1.0570f}};
	private static double m_inv[][] = {{0.4124f, 0.3576f, 0.1805f}, 
									  {0.2126f, 0.7152f, 0.0722f}, 
									  {0.0193f, 0.1192f, 0.9505f}};
	private static double refX = 0.95047f;
	private static double refY = 1.00000f;
	private static double refZ = 1.08883f;
	private static double refU = 0.19784f;
	private static double refV = 0.46834f;
	private static double lab_e = 0.008856f;
	private static double lab_k = 903.3f;

	private static double maxChroma(double L, double H){

		double C, bottom, cosH, hrad, lbottom, m1, m2, m3, rbottom, result, sinH, sub1, sub2, t, top;
		int _i, _j, _len, _len1;
		double row[];
		double _ref[] = {0.0f, 1.0f};


		hrad = (double) ((H / 360.0f) * 2 * PI);
		sinH = (double) (Math.sin(hrad));
		cosH = (double) (Math.cos(hrad));
		sub1 = (double) (Math.pow(L + 16, 3) / 1560896.0);
		sub2 = sub1 > 0.008856 ? sub1 : (double) (L / 903.3);
		result = Double.POSITIVE_INFINITY;
		for (_i = 0, _len = 3; _i < _len; ++_i) {
			row = m[_i];
			m1 = row[0];
			m2 = row[1];
			m3 = row[2];
			top = (double) ((0.99915 * m1 + 1.05122 * m2 + 1.14460 * m3) * sub2);
			rbottom = (double) (0.86330 * m3 - 0.17266 * m2);
			lbottom = (double) (0.12949 * m3 - 0.38848 * m1);
			bottom = (rbottom * sinH + lbottom * cosH) * sub2;

			for (_j = 0, _len1 = 2; _j < _len1; ++_j) {
				t = _ref[_j];
				C = (double) (L * (top - 1.05122 * t) / (bottom + 0.17266 * sinH * t));
				if ((C > 0 && C < result)) {
					result = C;
				}
			}
		}
		return result;
	}

	private static double dotProduct(double a[], double b[], int len){

		int i, _i, _ref;
		double ret = 0.0f;
		for (i = _i = 0, _ref = len - 1;    0 <= _ref ? _i <= _ref : _i >= _ref;    i = 0 <= _ref ? ++_i : --_i) {
			ret += a[i] * b[i];
		}
		return ret;

	}

	private static double round( double num, int places )
	{
		double n;
		n = (double) (Math.pow(10.0f, places));
		return (double) (Math.floor(num * n) / n);
	}

	private static double f( double t )
	{
		if (t > lab_e) {
			return (double) (Math.pow(t, 1.0f / 3.0f));
		} else {
			return (double) (7.787 * t + 16 / 116.0);
		}
	}

	private static double f_inv( double t )
	{
		if (Math.pow(t, 3) > lab_e) {
			return (double) (Math.pow(t, 3));
		} else {
			return (116 * t - 16) / lab_k;
		}
	}

	private static double fromLinear( double c )
	{
		if (c <= 0.0031308) {
			return 12.92f * c;
		} else {
			return (double) (1.055 * Math.pow(c, 1 / 2.4f) - 0.055);
		}
	}

	private static double toLinear( double c )
	{
		double a = 0.055f;

		if (c > 0.04045) {
			return (double) (Math.pow((c + a) / (1 + a), 2.4f));
		} else {
			return (double) (c / 12.92);
		}
	}

	private static double[] rgbPrepare( double tuple[] )
	{
		int i;

		for(i = 0; i < 3; ++i){
			tuple[i] = round(tuple[i], 3);

			if (tuple[i] < 0 || tuple[i] > 1) {
				if(tuple[i] < 0)
					tuple[i] = 0;
				else
					tuple[i] = 1;
				//System.out.println("Illegal rgb value: " + tuple[i]);
			}

			tuple[i] = round(tuple[i]*255, 0);
		}

		return tuple;
	}

	private static double[] XYZ_RGB( double tuple[] )
	{
		double B, G, R;
		R = fromLinear(dotProduct(m[0], tuple, 3));
		G = fromLinear(dotProduct(m[1], tuple, 3));
		B = fromLinear(dotProduct(m[2], tuple, 3));

		tuple[0] = R;
		tuple[1] = G;
		tuple[2] = B;

		return tuple;
	}

	private static double[] RGB_XYZ( double tuple[] )
	{
		double B, G, R, X, Y, Z;
		double rgbl[] = new double[3];

		R = tuple[0];
		G = tuple[1]; 
		B = tuple[2];

		rgbl[0] = toLinear(R);
		rgbl[1] = toLinear(G);
		rgbl[2] = toLinear(B);

		X = dotProduct(m_inv[0], rgbl, 3);
		Y = dotProduct(m_inv[1], rgbl, 3);
		Z = dotProduct(m_inv[2], rgbl, 3);

		tuple[0] = X;
		tuple[1] = Y;
		tuple[2] = Z;

		return tuple;
	}

	private static double[] XYZ_LUV( double tuple[] )
	{
		double L, U, V, X, Y, Z, varU, varV;

		X = tuple[0]; 
		Y = tuple[1]; 
		Z = tuple[2];

		varU = (4 * X) / (X + (15.0f * Y) + (3 * Z));
		varV = (9 * Y) / (X + (15.0f * Y) + (3 * Z));
		L = 116 * f(Y / refY) - 16;
		U = 13 * L * (varU - refU);
		V = 13 * L * (varV - refV);

		tuple[0] = L;
		tuple[1] = U;
		tuple[2] = V;

		return tuple;
	}

	private static double[] LUV_XYZ( double tuple[] )
	{
		double L, U, V, X, Y, Z, varU, varV, varY;

		L = tuple[0]; 
		U = tuple[1]; 
		V = tuple[2];

		if (L == 0) {
			tuple[2] = tuple[1] = tuple[0] = 0.0f;
			return tuple;
		}

		varY = f_inv((L + 16) / 116.0f);
		varU = U / (13.0f * L) + refU;
		varV = V / (13.0f * L) + refV;
		Y = varY * refY;
		X = 0 - (9 * Y * varU) / ((varU - 4.0f) * varV - varU * varV);
		Z = (9 * Y - (15 * varV * Y) - (varV * X)) / (3.0f * varV);

		tuple[0] = X;
		tuple[1] = Y;
		tuple[2] = Z;

		return tuple;
	}

	private static double[] LUV_LCH( double tuple[] )
	{
		double C, H, Hrad, L, U, V;

		L = tuple[0]; 
		U = tuple[1]; 
		V = tuple[2];

		C = (double) (Math.pow(Math.pow(U, 2) + Math.pow(V, 2), (1 / 2.0f)));
		Hrad = (double) (Math.atan2(V, U));
		H = (double) (Hrad * 360.0f / 2.0f / PI);
		if (H < 0) {
			H = 360 + H;
		}

		tuple[0] = L;
		tuple[1] = C;
		tuple[2] = H;

		return tuple;
	}

	private static double[] LCH_LUV( double tuple[] )
	{
		double C, H, Hrad, L, U, V;

		L = tuple[0]; 
		C = tuple[1]; 
		H = tuple[2];

		Hrad = (double) (H / 360.0 * 2.0 * PI);
		U = (double) (Math.cos(Hrad) * C);
		V = (double) (Math.sin(Hrad) * C);

		tuple[0] = L;
		tuple[1] = U;
		tuple[2] = V;

		return tuple;
	}

	private static double[] HUSL_LCH( double tuple[] )
	{
		double C, H, L, S, max;

		H = tuple[0]; 
		S = tuple[1]; 
		L = tuple[2];

		max = maxChroma(L, H);
		C = max / 100.0f * S;

		tuple[0] = L;
		tuple[1] = C;
		tuple[2] = H;

		return tuple;
	}

	private static double[] LCH_HUSL( double tuple[] )
	{
		double C, H, L, S, max;

		L = tuple[0]; 
		C = tuple[1]; 
		H = tuple[2];

		max = maxChroma(L, H);
		S = C / max * 100;

		tuple[0] = H;
		tuple[1] = S;
		tuple[2] = L;

		return tuple;
	}

}
