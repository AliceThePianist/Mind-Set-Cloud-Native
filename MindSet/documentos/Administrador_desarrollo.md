# Administrador de desarrollo

Este procedimiento permite a un operador autorizado promover una cuenta de desarrollo concreta a ADMINISTRADOR. No crea cuentas por defecto, no almacena contraseñas y no habilita un endpoint público de administración. Se preparó sin ejecutarlo contra las bases existentes; su comprobación operativa queda para la sesión de pruebas integrada.

## Preparación de la cuenta

Usar una cuenta de pruebas dedicada. Si ya existe, verificar su ID y correo y pasar a la promoción. Si no existe, cuando se habilite la sesión de pruebas, iniciar Usuarios y registrar una cuenta por el endpoint público normal. Desde PowerShell:

```powershell
$credencialMindSet = Get-Credential -Message 'Correo y contraseña privada de la cuenta de desarrollo'
$cuentaMindSet = Invoke-RestMethod -Method Post -Uri 'http://localhost:8081/api/usuarios/registro' -ContentType 'application/json' -Body (@{
    correo = $credencialMindSet.UserName
    contrasena = $credencialMindSet.GetNetworkCredential().Password
} | ConvertTo-Json)
$cuentaMindSet | Select-Object id, correo, rol
Remove-Variable credencialMindSet
```

La contraseña se introduce en el diálogo, no en el historial de comandos ni en el SQL. Usuarios aplica BCrypt. Guardarla en el gestor privado habitual; no copiarla a esta documentación. El registro siempre crea ESTUDIANTE aunque el cuerpo intente enviar otro rol o ID.

## Selección y simulación

Desde la carpeta `MindSet/`, con el Compose PostgreSQL existente en ejecución:

```powershell
$idAdminMindSet = Read-Host 'ID exacto de la cuenta de desarrollo'
if ($idAdminMindSet -notmatch '^[1-9][0-9]*$') { throw 'ID inválido' }
$correoAdminMindSet = (Read-Host 'Correo exacto de esa cuenta').Trim().ToLowerInvariant()

Get-Content -LiteralPath '.\scripts\promover-admin-desarrollo.sql' -Raw |
    docker compose exec -T postgres psql -X -U postgres -d usuarios_db -v ON_ERROR_STOP=1 -v "usuario_id=$idAdminMindSet" -v "correo=$correoAdminMindSet"
```

El script bloquea una fila que coincida simultáneamente con ID y correo y tenga rol ESTUDIANTE o ADMINISTRADOR. Si no existe esa coincidencia, se detiene; no selecciona otra cuenta. Solo muestra ID, correo y rol. La ejecución anterior termina con **ROLLBACK**: no guarda cambios.

El uso de `-U postgres` corresponde al entorno local de Compose existente y presupone que el operador tiene acceso autorizado a Docker. No es un mecanismo de aprovisionamiento productivo. No hay que exponer el puerto de PostgreSQL a otras máquinas ni poner su contraseña en la línea de comandos.

## Confirmación explícita del operador

Revisar los datos mostrados en la simulación. Solo si corresponden a la cuenta elegida, ejecutar:

```powershell
Get-Content -LiteralPath '.\scripts\promover-admin-desarrollo.sql' -Raw |
    docker compose exec -T postgres psql -X -U postgres -d usuarios_db -v ON_ERROR_STOP=1 -v "usuario_id=$idAdminMindSet" -v "correo=$correoAdminMindSet" -v confirmar=SI_PROMOVER
```

Solo el valor exacto `SI_PROMOVER` ejecuta COMMIT; cualquier otro valor vuelve a ROLLBACK. El script revalida y bloquea la cuenta en cada ejecución. Repetirlo sobre el mismo administrador conserva su rol y contraseña. No promueve cuentas profesionales para evitar mezclar identidades de atención y administración.

Después iniciar sesión de nuevo mediante `POST /api/usuarios/login`. El JWT nuevo debe contener `rol=ADMINISTRADOR` y el ID elegido. Usar ese token para `POST /api/usuarios/profesionales` con correo y contraseña privados de la nueva cuenta profesional. La respuesta devuelve PROFESIONAL sin hash ni contraseña. El token no debe guardarse en Git ni en capturas compartidas.

## Alcance y comprobación posterior

El SQL solo actualiza `rol` de la fila elegida: no altera contraseñas, no borra usuarios y no toca otras bases. Está en `scripts/`, separado de los scripts de inicialización de PostgreSQL; Docker no lo ejecuta al arrancar.

Las comprobaciones pendientes en la sesión funcional son: login administrativo, alta profesional 201, estudiante/profesional rechazados con 403 y petición sin token rechazada con 401. Las pruebas unitarias del registro confirman que ID y rol del cuerpo no controlan la entidad persistida; la cadena de permisos HTTP deberá probarse cuando se habiliten las pruebas funcionales.
