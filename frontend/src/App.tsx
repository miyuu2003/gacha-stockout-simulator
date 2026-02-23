import { useState } from "react";

function App() {
  const [result, setResult] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const runSimulation = async () => {
    setError(null);
    setResult(null);
    setLoading(true);

    try {
      // Vite proxy (/api -> http://localhost:8080) を使う
      const response = await fetch("/api/v1/simulations", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          productName: "テストガチャ",
          popularity: "HIGH",
          releaseAt: "2026-02-10T10:00:00+09:00",
          storeType: "LARGE",
          initialStock: 120,
          snsBoostEnabled: true,
          simulationHours: 24,
          timeBucketMinutes: 30,
          runs: 1000,
        }),
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(`${response.status} ${data?.code ?? "REQUEST_FAILED"}`);
      }
      setResult(data as Record<string, unknown>);
    } catch (e) {
      const message = e instanceof Error ? e.message : "Unknown error";
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>ガチャ枯渇シミュレーター</h1>
      <button onClick={runSimulation} disabled={loading}>
        {loading ? "実行中..." : "シミュレーション実行"}
      </button>

      {error && <p style={{ color: "crimson" }}>Error: {error}</p>}

      {result && (
        <pre>{JSON.stringify(result, null, 2)}</pre>
      )}
    </div>
  );
}

export default App;
