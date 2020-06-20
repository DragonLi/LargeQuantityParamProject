package com.asiainfo.cem.satisfaction.Utils.TargetFIlterUtils.objectalgebra;

public class QueryBuilder extends PredSqlBuilder implements QueryAlg<SqlStatement,SqlGenerator> {

    @Override
    public SqlStatement and(SqlStatement left, SqlStatement right) {
        return new SqlStatement() {
            @Override
            public String generate() {
                return left.generate()+" and "+right.generate();
            }
        };
    }

    @Override
    public SqlStatement or(SqlStatement left, SqlStatement right) {
        return new SqlStatement() {
            @Override
            public String generate() {
                return "("+left.generate()+" or "+right.generate()+")";
            }
        };
    }

    @Override
    public SqlStatement queryWith(String dbField, SqlGenerator sqlGenerator) {
        return new SqlStatement() {
            @Override
            public String generate() {
                return sqlGenerator.gen(dbField);
            }
        };
    }
}
