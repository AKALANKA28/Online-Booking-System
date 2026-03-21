import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{ts,tsx}",
    "./components/**/*.{ts,tsx}",
    "./lib/**/*.{ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        paper: "#F6F1E9",
        cloud: "#FBF8F3",
        ink: "#13151B",
        smoke: "#5D6472",
        line: "#D8D0C4",
        cobalt: "#2459FF",
        cobaltSoft: "#E7ECFF",
        ember: "#E86C4D",
        emberSoft: "#FDE7E1",
        pine: "#1E7855",
        pineSoft: "#DEF4EA",
        plum: "#7546F1",
        plumSoft: "#EFE8FF",
        gold: "#BA8E2E",
        goldSoft: "#F6EAC9",
      },
      boxShadow: {
        soft: "0 20px 50px rgba(15, 17, 21, 0.10)",
        panel: "0 16px 32px rgba(8, 13, 26, 0.10)",
      },
      borderRadius: {
        card: "28px",
      },
      fontFamily: {
        sans: ["var(--font-manrope)", "system-ui", "sans-serif"],
        display: ["var(--font-space)", "system-ui", "sans-serif"],
      },
      backgroundImage: {
        "paper-grid": "linear-gradient(rgba(19,21,27,0.04) 1px, transparent 1px), linear-gradient(90deg, rgba(19,21,27,0.04) 1px, transparent 1px)",
      },
    },
  },
  plugins: [],
};

export default config;
