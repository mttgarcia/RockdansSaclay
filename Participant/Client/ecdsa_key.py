from ecdsa import SigningKey, NIST224p
sk = SigningKey.generate(curve = NIST224p)
vk = sk.get_verifying_key()
open("private.pem","w").write(sk.to_pem().decode())
open("public.pem","w").write(vk.to_pem().decode())