package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils;

public enum CemQueryParamCompositionMode {
    SingleAnd,//只有一个内部值，并与其他条件“与”组合
    OrMultipartAnd,//内部有多个值，用“或”连起来，作为一个整体，再与其他条件用“与”组合
}
