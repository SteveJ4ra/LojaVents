UPDATE reservas
SET estado = 'CONFIRMADA',
    actualizado_en = NOW()
WHERE estado = 'COMPLETADA';
