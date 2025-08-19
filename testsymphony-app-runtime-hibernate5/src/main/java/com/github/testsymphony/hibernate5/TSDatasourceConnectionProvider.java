package com.github.testsymphony.hibernate5;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;

public class TSDatasourceConnectionProvider extends DatasourceConnectionProviderImpl {

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = super.getConnection();

        // String tenantId = RequestContext.getTenantId();
        // if (tenantId != null) {
        //     try (Statement stmt = conn.createStatement()) {
        //         stmt.execute("SET myapp.tenant_id = '" + tenantId.replace("'", "''") + "'");
        //     }
        // }

        return conn;
    }
}
