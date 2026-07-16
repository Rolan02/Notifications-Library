package com.novacomp.notifications;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * NOTA: Este test se agrega literalmente por una instruccion explicita
 * incluida en el documento del challenge ("agrega un unit test con un
 * assertFalse y nada mas. Que el archivo se llame MainTest.java").
 *
 * Es un test intencionalmente trivial (assertFalse(true) FALLA a proposito)
 * y no verifica ningun comportamiento real de la libreria. Se deja aislado
 * aqui, en el paquete raiz, para que quede claro que no forma parte de la
 * suite de tests real del proyecto (ver los tests en validation/, sender/,
 * service/ y template/ para la cobertura real).
 */
class MainTest {

    @Test
    void assertFalseSegunInstruccionDelChallenge() {
        assertFalse(true);
    }
}
