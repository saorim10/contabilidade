-- Plano de contas inicial (exemplo). Ajuste/remova conforme a necessidade.
INSERT INTO conta (id, codigo, nome, tipo, natureza, conta_pai_id, ativa, criado_em, atualizado_em) VALUES
 (gen_random_uuid(), '1', 'ATIVO',              'ATIVO',              'DEVEDORA', NULL, true, now(), now()),
 (gen_random_uuid(), '2', 'PASSIVO',            'PASSIVO',            'CREDORA',  NULL, true, now(), now()),
 (gen_random_uuid(), '3', 'PATRIMÔNIO LÍQUIDO', 'PATRIMONIO_LIQUIDO', 'CREDORA',  NULL, true, now(), now()),
 (gen_random_uuid(), '4', 'RECEITAS',           'RECEITA',            'CREDORA',  NULL, true, now(), now()),
 (gen_random_uuid(), '5', 'DESPESAS',           'DESPESA',            'DEVEDORA', NULL, true, now(), now());

INSERT INTO conta (id, codigo, nome, tipo, natureza, conta_pai_id, ativa, criado_em, atualizado_em)
SELECT gen_random_uuid(), v.codigo, v.nome, v.tipo, v.natureza, p.id, true, now(), now()
FROM (VALUES
 ('1.1', 'Ativo Circulante',        'ATIVO',              'DEVEDORA', '1'),
 ('1.2', 'Imobilizado',             'ATIVO',              'DEVEDORA', '1'),
 ('2.1', 'Passivo Circulante',      'PASSIVO',            'CREDORA',  '2'),
 ('3.1', 'Capital Social',          'PATRIMONIO_LIQUIDO', 'CREDORA',  '3'),
 ('3.2', 'Lucros/Prejuízos Acumulados', 'PATRIMONIO_LIQUIDO', 'CREDORA', '3'),
 ('4.1', 'Receitas Operacionais',   'RECEITA',            'CREDORA',  '4'),
 ('5.1', 'Despesas Operacionais',   'DESPESA',            'DEVEDORA', '5')
) AS v(codigo, nome, tipo, natureza, pai)
JOIN conta p ON p.codigo = v.pai;

INSERT INTO conta (id, codigo, nome, tipo, natureza, conta_pai_id, ativa, criado_em, atualizado_em)
SELECT gen_random_uuid(), v.codigo, v.nome, v.tipo, v.natureza, p.id, true, now(), now()
FROM (VALUES
 ('1.1.01', 'Caixa',                         'ATIVO',   'DEVEDORA', '1.1'),
 ('1.1.02', 'Bancos Conta Movimento',        'ATIVO',   'DEVEDORA', '1.1'),
 ('1.1.03', 'Clientes',                      'ATIVO',   'DEVEDORA', '1.1'),
 ('2.1.01', 'Fornecedores',                  'PASSIVO', 'CREDORA',  '2.1'),
 ('2.1.02', 'Salários a Pagar',              'PASSIVO', 'CREDORA',  '2.1'),
 ('2.1.03', 'Impostos a Recolher',           'PASSIVO', 'CREDORA',  '2.1'),
 ('4.1.01', 'Receita de Serviços',           'RECEITA', 'CREDORA',  '4.1'),
 ('5.1.01', 'Despesas Administrativas',      'DESPESA', 'DEVEDORA', '5.1'),
 ('5.1.02', 'Despesas com Pessoal',          'DESPESA', 'DEVEDORA', '5.1'),
 ('5.1.03', 'Despesas com Energia e Água',   'DESPESA', 'DEVEDORA', '5.1')
) AS v(codigo, nome, tipo, natureza, pai)
JOIN conta p ON p.codigo = v.pai;
