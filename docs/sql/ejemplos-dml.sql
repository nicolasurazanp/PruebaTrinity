-- Ejemplos de DML equivalentes a las operaciones de la API.
-- Se ejecutan dentro de una transacción y se deshacen al final (ROLLBACK),
-- así que no dejan datos en la base.

BEGIN;

-- Crear un cliente (POST /api/clientes)
INSERT INTO clientes (tipo_identificacion, numero_identificacion, nombres, apellido,
                      correo_electronico, fecha_nacimiento, fecha_creacion)
VALUES ('CC', '1020304050', 'Ana', 'Perez', 'ana@correo.com', '1995-05-20', NOW());

-- Crear dos cuentas activas con saldo 0 (POST /api/cuentas)
INSERT INTO cuentas (tipo_cuenta, numero_cuenta, estado, saldo, exenta_gmf, cliente_id, fecha_creacion, version)
VALUES ('AHORROS', '5300000001', 'ACTIVA', 0.00, FALSE,
        (SELECT id FROM clientes WHERE numero_identificacion = '1020304050'), NOW(), 0),
       ('CORRIENTE', '3300000002', 'ACTIVA', 0.00, FALSE,
        (SELECT id FROM clientes WHERE numero_identificacion = '1020304050'), NOW(), 0);

-- Consignar $100.000 en la cuenta de ahorros (POST /api/transacciones/consignaciones)
UPDATE cuentas
SET saldo = saldo + 100000.00, fecha_modificacion = NOW(), version = version + 1
WHERE numero_cuenta = '5300000001';

INSERT INTO movimientos (codigo_transaccion, cuenta_id, tipo_transaccion, tipo_movimiento, monto, saldo_resultante, fecha)
SELECT gen_random_uuid()::text, id, 'CONSIGNACION', 'CREDITO', 100000.00, saldo, NOW()
FROM cuentas WHERE numero_cuenta = '5300000001';

-- Transferir $40.000 de ahorros a corriente (POST /api/transacciones/transferencias)
-- El débito exige saldo suficiente: si no se cumple, no se actualiza ninguna fila.
UPDATE cuentas
SET saldo = saldo - 40000.00, fecha_modificacion = NOW(), version = version + 1
WHERE numero_cuenta = '5300000001' AND estado = 'ACTIVA' AND saldo >= 40000.00;

UPDATE cuentas
SET saldo = saldo + 40000.00, fecha_modificacion = NOW(), version = version + 1
WHERE numero_cuenta = '3300000002' AND estado = 'ACTIVA';

INSERT INTO movimientos (codigo_transaccion, cuenta_id, tipo_transaccion, tipo_movimiento, monto, saldo_resultante, fecha)
SELECT 'transferencia-ejemplo', id, 'TRANSFERENCIA',
       CASE WHEN numero_cuenta = '5300000001' THEN 'DEBITO' ELSE 'CREDITO' END,
       40000.00, saldo, NOW()
FROM cuentas WHERE numero_cuenta IN ('5300000001', '3300000002');

-- Inactivar una cuenta (PATCH /api/cuentas/{id}/inactivar)
UPDATE cuentas SET estado = 'INACTIVA', fecha_modificacion = NOW(), version = version + 1
WHERE numero_cuenta = '3300000002' AND estado <> 'CANCELADA';

-- Estado de cuenta (GET /api/cuentas/{id}/estado-cuenta)
SELECT m.fecha, m.tipo_transaccion, m.tipo_movimiento, m.monto, m.saldo_resultante
FROM movimientos m
JOIN cuentas c ON c.id = m.cuenta_id
WHERE c.numero_cuenta = '5300000001'
ORDER BY m.fecha, m.id;

-- ¿El cliente tiene productos? Si devuelve TRUE, no se puede eliminar.
SELECT EXISTS (
    SELECT 1 FROM cuentas c
    JOIN clientes cl ON cl.id = c.cliente_id
    WHERE cl.numero_identificacion = '1020304050'
) AS tiene_productos;

ROLLBACK;
