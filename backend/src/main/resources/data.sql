-- Sample data for testing. Real interactions are fetched live from OpenFDA.

INSERT INTO drugs (name, active_substance, description, side_effects) VALUES
('Advil', 'Ibuprofen', 'Nonsteroidal anti-inflammatory drug used for pain and fever.', 'Stomach upset, heartburn, dizziness, mild rash.'),
('Aspirin', 'Aspirin', 'Salicylate used for pain relief and as an antiplatelet.', 'GI bleeding, tinnitus, allergic reactions.'),
('Glucophage', 'Metformin', 'First-line oral medication for type 2 diabetes.', 'Nausea, diarrhea, lactic acidosis (rare).'),
('Altace', 'Ramipril', 'ACE inhibitor used for hypertension and heart failure.', 'Dry cough, dizziness, hyperkalemia.'),
('Coumadin', 'Warfarin', 'Anticoagulant used to prevent thrombosis and embolism.', 'Bleeding, bruising, skin necrosis (rare).'),
('Tylenol', 'Paracetamol', 'Analgesic and antipyretic for mild to moderate pain.', 'Liver damage at high doses, rash.'),
('Lipitor', 'Atorvastatin', 'Statin used to lower cholesterol and prevent cardiovascular events.', 'Muscle pain, elevated liver enzymes.'),
('Prilosec', 'Omeprazole', 'Proton pump inhibitor used to reduce stomach acid.', 'Headache, nausea, vitamin B12 deficiency long-term.'),
('Zoloft', 'Sertraline', 'SSRI antidepressant used for depression and anxiety.', 'Insomnia, nausea, sexual dysfunction.'),
('Amoxil', 'Amoxicillin', 'Penicillin antibiotic used for bacterial infections.', 'Rash, diarrhea, allergic reactions.');

INSERT INTO interactions (drug1, drug2, severity, description, llm_explanation) VALUES
('Warfarin', 'Aspirin', 'SEVERE',
 'Concurrent use significantly increases the risk of major bleeding due to combined anticoagulant and antiplatelet effects.',
 'Taking Warfarin and Aspirin together greatly increases your risk of bleeding, including dangerous internal bleeding. This combination should only be used under close medical supervision.'),
('Ibuprofen', 'Ramipril', 'MODERATE',
 'NSAIDs may reduce the antihypertensive effect of ACE inhibitors and increase risk of renal impairment.',
 'Ibuprofen can make Ramipril less effective at controlling your blood pressure and may strain your kidneys. Use the lowest effective dose for the shortest time, and tell your doctor.'),
('Ibuprofen', 'Aspirin', 'MODERATE',
 'Ibuprofen may interfere with the antiplatelet effect of low-dose aspirin and increases GI bleeding risk.',
 'Combining Ibuprofen and Aspirin can reduce Aspirin''s heart-protective benefit and raise the risk of stomach bleeding. Speak to your doctor before taking them together regularly.'),
('Metformin', 'Atorvastatin', 'MILD',
 'No clinically significant interaction; both may be used together for diabetic patients with dyslipidemia.',
 'Metformin and Atorvastatin are commonly prescribed together. There is no significant interaction, but always follow your doctor''s instructions.'),
('Paracetamol', 'Amoxicillin', 'MILD',
 'No clinically significant interaction reported.',
 'Paracetamol and Amoxicillin can usually be taken together safely. Follow the dosing instructions on each medication.');
