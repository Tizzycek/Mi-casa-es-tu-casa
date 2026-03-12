package it.tizzycek.micasaestucasa.data.utente

enum class Stato(val code: Int) {
    OK(1),
    SOSPESO(0),
    BANNATO(-1)
}