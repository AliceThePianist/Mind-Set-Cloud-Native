-- Uso exclusivo de un operador autorizado en usuarios_db. Ejecutar con psql.
-- Variables requeridas: usuario_id, correo. confirmar=SI_PROMOVER confirma; cualquier otro valor revierte.
\set ON_ERROR_STOP on
\if :{?usuario_id}
\else
  \echo 'Falta la variable usuario_id'
  \quit
\endif
\if :{?correo}
\else
  \echo 'Falta la variable correo'
  \quit
\endif
\if :{?confirmar}
\else
  \set confirmar NO
\endif

BEGIN;
-- Si no hay exactamente una coincidencia, gset falla y la conexión revierte la transacción.
SELECT id, correo, rol
FROM usuarios
WHERE id = :'usuario_id'::bigint
  AND correo = :'correo'
  AND rol IN ('ESTUDIANTE', 'ADMINISTRADOR')
FOR UPDATE
\gset cuenta_

\echo 'Cuenta seleccionada:' :cuenta_id :cuenta_correo 'Rol actual:' :cuenta_rol
UPDATE usuarios SET rol = 'ADMINISTRADOR'
WHERE id = :'cuenta_id'::bigint AND correo = :'cuenta_correo'
RETURNING id, correo, rol;

SELECT :'confirmar' = 'SI_PROMOVER' AS aplicar \gset
\if :aplicar
  COMMIT;
  \echo 'Cambio confirmado. Iniciar sesión nuevamente para obtener el JWT administrativo.'
\else
  ROLLBACK;
  \echo 'SIMULACION: no se guardó ningún cambio. Revisar ID y correo antes de confirmar.'
\endif
