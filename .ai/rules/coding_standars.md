# Estándares de Código

## General

El código y nombres técnicos deberán seguir las convenciones estándar de cada
lenguaje.

La documentación funcional del proyecto se escribe en español.

---

# Java

## Formato

- UTF-8.
- Indentación de 4 espacios.
- Máximo recomendado de 120 caracteres por línea.
- Utilizar el estilo estándar de IntelliJ IDEA para Java.
- Separar bloques lógicos mediante líneas en blanco.

El límite de 120 caracteres es una guía fuerte, no una razón para producir
código menos legible.

---

# Nombres

Utilizar nombres descriptivos.

Evitar abreviaturas ambiguas.

Preferir:

```java
calculateTotalAmount()
```

sobre:

```java
calcTot()
```

---

# Tipos

Preferir tipos explícitos sobre `var`.

`var` solo podrá utilizarse cuando el tipo sea inmediatamente evidente y
mejore objetivamente la legibilidad.

---

# Inmutabilidad

Preferir objetos inmutables cuando resulte práctico.

Utilizar `final` en campos que no deban reasignarse.

No es obligatorio declarar `final` en todos los parámetros y variables
locales.

Evitar mutaciones innecesarias dentro de Streams.

---

# Constantes

Evitar números y cadenas mágicas cuando representen conceptos de negocio,
configuración o valores reutilizados.

No crear constantes para valores triviales que solo aparecen una vez y cuyo
significado sea evidente.

---

# Condicionales

Preferir early return cuando reduzca anidamiento.

Evitar `else` cuando el flujo pueda expresarse claramente mediante retorno
temprano.

Cuando una condición compleja represente un concepto reconocible, extraerla a
una variable o método con nombre descriptivo.

---

# Null

Preferir:

```java
value == null
value != null
```

sobre:

```java
Objects.isNull(value)
Objects.nonNull(value)
```

para comprobaciones directas.

Diseñar APIs para minimizar la necesidad de comprobaciones defensivas de
`null`.

---

# Excepciones

Utilizar excepciones unchecked para errores de dominio y aplicación cuando sea
apropiado.

No utilizar `throws Exception`.

No capturar `Exception` genéricamente salvo en fronteras donde exista una
justificación clara.

---

# Override

Utilizar siempre `@Override` al sobrescribir métodos.

---

# Comentarios

El código debe intentar explicarse mediante nombres y estructura.

No utilizar comentarios para explicar código innecesariamente complejo:
simplificar primero el código.

Los comentarios son apropiados para explicar el POR QUÉ cuando no sea evidente.

También pueden utilizarse para:

- expresiones regulares complejas;
- cron expressions;
- TODOs concretos;
- given/when/then en pruebas.

No dejar código comentado.