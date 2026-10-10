# GCA 1-hour cram — how to think, then the stub

Not a Capital One question dump. Same **slots** keep showing up. **70 min:** Q1 → Q2 → Q4 → **Q3 last** (grids eat the clock).

In one hour: read the **breakdowns**, then retype **one** stub per type. The code is only proof you understood the steps.

---

## 1. Linear scan / fixed window (Q1)

**What they’re asking:** For every position, look at a **small neighborhood** (this cell and the next 1–2) and write a yes/no or a count. Zigzag triples: output[i] is about `(a[i], a[i+1], a[i+2])` only.

**How you know:** “consecutive”, “each triple/pair”, “for every i”, length of answer is `n-2` or `n-1`.

**Steps**

1. If the window is size `k`, the last valid start index is `n-k`. Loop `i in 0 until n-k+1` (zigzag: `k=3` → `until n-2`).
2. Name the locals (`a,b,c`) so the boolean matches the spec **word for word**.
3. Zigzag is **peak or valley**: middle is strictly above both neighbors **or** strictly below both. `1,2,2` is **not** a zigzag (`2 > 2` is false).
4. Push `1`/`0` (or the computed value). Don’t mutate the input unless asked.

**Walkthrough:** `[1, 2, 1, 3, 4]`

- i=0: 1<2>1 → peak → 1  
- i=1: 2>1<3 → valley → 1  
- i=2: 1<3<4 → increasing, not zig → 0  

**Pitfalls:** off-by-one (`until` vs `..`); `n<3` → empty list; using `<=` when the spec said `<`.

```kotlin
fun zigzags(a: List<Int>): List<Int> {
    val out = mutableListOf<Int>()
    for (i in 0 until a.size - 2) {
        val x = a[i]; val y = a[i + 1]; val z = a[i + 2]
        val zig = (x < y && y > z) || (x > y && y < z)
        out.add(if (zig) 1 else 0)
    }
    return out
}
```

---

## 2. Hash map / set (Q2, sometimes Q4)

**What they’re asking:** You need a **memory of the past** — “have I seen the partner of this value?”, “how many times did X appear?”, “first index of Y?”. Brute force is a nested loop; the map makes it **one pass**.

**How you know:** “pair that sums to”, “duplicate”, “frequency”, “anagram”, “first unique”. If you’d write `for i, for j > i`, you probably want a map.

**Steps (Two Sum shape)**

1. Decide the **key** (the number) and **value** (index, or count).
2. For each `x` at `i`: what would complete the answer? `need = target - x`.
3. **Look up `need` first**, then **record `x`**. If you record first, `x` pairs with itself.
4. Set is the same idea when you only care “present / not”.

**Walkthrough:** `nums = [2,7,11,15]`, target 9  

- i=0, x=2, need=7, map empty → store 2→0  
- i=1, x=7, need=2, map has 2 at 0 → `[0,1]`

**Pitfalls:** overwrite vs keep first index; `getOrDefault`; iterating a map you mutate.

```kotlin
fun twoSum(nums: IntArray, target: Int): IntArray {
    val idx = mutableMapOf<Int, Int>()
    for (i in nums.indices) {
        val need = target - nums[i]
        val j = idx[need]
        if (j != null) return intArrayOf(j, i)
        idx[nums[i]] = i
    }
    return intArrayOf()
}
```

---

## 3. Two pointers / sliding window (Q2–Q3)

**What they’re asking:** A **contiguous** stretch (subarray/substring). You want longest, shortest, or count of windows that satisfy a budget (at most K distinct, sum ≥ S, no repeats).

**Two flavors**

- **Opposite ends** (sorted array, palindrome, 2-sum on sorted): `l` at start, `r` at end. If sum too small, `l++`; too big, `r--`.  
- **Same direction window:** `r` only moves right (always add `a[r]`). `l` crawls right **only when the window is illegal**, throwing `a[l]` out. The window `[l, r]` is always the tightest legal one ending at `r`.

**Steps (longest with at most K distinct chars)**

1. `r` walks the string; add `s[r]` into a counter.  
2. If you now have **too many** distinct, `while` illegal: remove `s[l]`, `l++`.  
3. After the while, `[l,r]` is valid → update `best = r-l+1`.  
4. Never move `l` backward. That’s why it’s O(n).

**Walkthrough:** `s = "eceba"`, K=2  

- e,c,e → 2 distinct, len 3  
- +b → 3 distinct → shrink from left until 2 (`eba`)  
- best stays 3 (`ece`)

**Pitfalls:** updating `best` *inside* the while (wrong); `l` going past `r`; forgetting to decrement the count to 0 before dropping “distinct”.

**When not this:** non-contiguous subsequence → often DP or greedy, not this window.

```kotlin
fun maxWindowAtMostK(s: String, k: Int): Int {
    val cnt = IntArray(256)
    var l = 0
    var best = 0
    var used = 0
    for (r in s.indices) {
        if (cnt[s[r].code]++ == 0) used++
        while (used > k) {
            if (--cnt[s[l].code] == 0) used--
            l++
        }
        best = maxOf(best, r - l + 1)
    }
    return best
}
```

---

## 4. Grid: simulation vs BFS (Q3)

**Two different questions people mix up.**

### A) Simulation (spiral, “fall down”, rotate the box)

You’re **not** searching. You **walk a path** or **apply gravity** with nested loops.

**Steps:** pick a start, a direction; when the next cell is out of bounds or visited, **turn**; stop when you’ve visited `n*m` cells. Or: for each column, compact rocks/zeros. Slow to type, easy to mess up bounds — that’s why it’s last.

### B) BFS (shortest path, rotting oranges, islands with “spread”)

You’re **searching** in an unweighted grid: every step costs 1.

**Steps**

1. **Walls / start blocked** → fail immediately.  
2. Queue holds `(row, col, distance)`. Mark **visited when you enqueue** (or you’ll queue the same cell 4 times).  
3. Four deltas: `±1` row or `±1` col, not diagonals unless asked.  
4. Skip out of bounds, walls, already seen.  
5. First time you **pop** the target, that distance is shortest. If the queue dies, no path.

**Islands:** BFS/DFS from every unvisited land, increment count, flood the island. Same deltas, no distance.

**Walkthrough:** 3×3 open grid, start (0,0), end (2,2), 4-dir. First reach end at distance 5 (Manhattan). If a wall sits on the only path, queue empties → `-1`.

**Pitfalls:** DFS for shortest path (wrong on grids); not marking visited; `ArrayDeque.removeFirst()` vs stack.

```kotlin
fun shortest(grid: Array<IntArray>): Int {
    val n = grid.size
    val m = grid[0].size
    if (grid[0][0] == 1 || grid[n - 1][m - 1] == 1) return -1
    val q = ArrayDeque<IntArray>()
    val seen = Array(n) { BooleanArray(m) }
    q.add(intArrayOf(0, 0, 1))
    seen[0][0] = true
    val dr = intArrayOf(1, -1, 0, 0)
    val dc = intArrayOf(0, 0, 1, -1)
    while (q.isNotEmpty()) {
        val (r, c, d) = q.removeFirst()
        if (r == n - 1 && c == m - 1) return d
        for (k in 0..3) {
            val nr = r + dr[k]
            val nc = c + dc[k]
            if (nr !in 0 until n || nc !in 0 until m) continue
            if (seen[nr][nc] || grid[nr][nc] == 1) continue
            seen[nr][nc] = true
            q.add(intArrayOf(nr, nc, d + 1))
        }
    }
    return -1
}
```

---

## 5. Stack: “next greater” (Q4)

**What they’re asking:** For each position, the **nearest** later (or earlier) element that is **bigger** (or smaller). Nested `for i, for j > i` is O(n²). A stack of **indices** whose values are waiting for a better candidate is O(n).

**Mental model:** people in a line facing right. Each new person is taller than some of the people still waiting — those shorter people **finally get their answer** (this new height) and leave the line. People still in the stack never saw anyone taller.

**Steps**

1. Stack stores **indexes**, not values (you need position + `a[i]`). Keep it **monotonic** (e.g. increasing heights).  
2. For each new `i`: **while** the stack top is **worse** than `a[i]` (strictly shorter if we want next greater), pop it and write `ans[popped] = a[i]`.  
3. Push `i`.  
4. Whoever is left at the end never found a greater → `-1` (or whatever the spec says).

**Walkthrough:** `[2, 1, 2, 4]` next greater  

- 2 waiting  
- 1 < 2, 1 waits  
- 2: pops 1 (next greater of 1 is 2); 2 equals stack top 2, don’t pop; push  
- 4: pops both 2s → their next greater is 4  
- answer `[4, 2, 4, -1]`

**Parentheses** is the same structure: stack of unmatched `'('` indexes; a `')'` pops. Leftover indexes are invalid.

**Histogram (84)** is “next smaller to left **and** right” then width × height — same stack, twice. Skip on a 1-hour crunch unless Q4 looks exactly like it.

**Pitfalls:** storing values instead of indexes; `<=` vs `<` (duplicates); using a queue.

```kotlin
fun nextGreater(a: IntArray): IntArray {
    val ans = IntArray(a.size) { -1 }
    val st = ArrayDeque<Int>()
    for (i in a.indices) {
        while (st.isNotEmpty() && a[st.last()] < a[i]) {
            ans[st.removeLast()] = a[i]
        }
        st.addLast(i)
    }
    return ans
}
```

---

## Defaults on the real test

- `n` too small → empty / 0, not a crash.  
- `1e5` elements → no O(n²) on Q1–Q2.  
- Spec said **strict** inequality → equals are false.  
- Q4: ugly O(n²) that **passes** beats a blank optimal.

**This hour, type:** window loop, two-sum map, BFS (or spiral). That’s enough.
