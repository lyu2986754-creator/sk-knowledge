# -*- coding: utf-8 -*-
"""Generate a clean, non-overlapping architecture diagram (.drawio + .png)."""

PAGE_W, PAGE_H = 1760, 1180

# layer background boxes: id, x, y, w, h, title, fill
LAYERS = [
    ("L1", 120, 40,  1500, 110, "① 用户与前端接入",           "#dae8fc"),
    ("L2", 120, 190, 1500, 110, "② 接入层 / 网关 (Spring Security + JWT)", "#ffe6cc"),
    ("L3", 120, 330, 1500, 230, "③ 业务服务层 (Service)",     "#d5e8d4"),
    ("L4", 120, 600, 1500, 95,  "④ 编排与文档处理 (Temporal + 解析切块)", "#e1d5e7"),
    ("L5", 120, 735, 1500, 110, "⑤ AI 与检索层 (Spring AI)",  "#fff2cc"),
    ("L6", 120, 880, 1500, 120, "⑥ 数据存储层",               "#f8cecc"),
    ("L7", 120, 1030,1500, 90,  "⑦ 基础设施与部署 (运行支撑)", "#d4e1f9"),
]

# nodes: id, x, y, w, h, label(multiline '\n'), fill, layer
NODES = [
    # L1
    ("U1", 140, 70, 280, 50, "普通用户 / 租户管理员", "#dae8fc"),
    ("U2", 520, 70, 520, 50, "sk-knowledge-frontend\n(Vue3 + Vite + Axios)", "#dae8fc"),
    # L2
    ("G1", 140, 215, 240, 55, "TokenFilter\n(JWT 拦截)", "#ffe6cc"),
    ("G2", 410, 215, 220, 55, "SecurityConfig", "#ffe6cc"),
    ("G3", 680, 200, 940, 85, "REST Controllers\n• KnowledgeBase • KnowledgeDoc • KnowledgeTrash\n• ChatCompletions • ChatModel • VectorDb", "#ffe6cc"),
    # L3 row1
    ("S1", 140, 355, 270, 55, "KnowledgeBaseService\n(知识库管理)", "#d5e8d4"),
    ("S2", 420, 355, 260, 55, "KnowledgeDocService\n(文档上传/编排)", "#d5e8d4"),
    ("S3", 710, 355, 250, 55, "KnowledgeTrashService\n(回收站/恢复)", "#d5e8d4"),
    ("S4", 1000,355, 270, 55, "ChatCompletionsService\n(RAG 对话)", "#d5e8d4"),
    ("S5", 1310,355, 250, 55, "ChatModelService\n(模型管理)", "#d5e8d4"),
    # L3 row2
    ("S8", 140, 465, 250, 55, "UserService + Role\n(用户与权限)", "#d5e8d4"),
    ("S7", 710, 465, 250, 55, "MinioService\n(对象存储)", "#d5e8d4"),
    ("S9", 1000,465, 240, 55, "TikaSemanticChunk\n(语义切块)", "#d5e8d4"),
    ("S6", 1280,465, 280, 55, "VectorService\n(向量检索门面)", "#d5e8d4"),
    # L4
    ("O5", 140, 615, 250, 60, "MapStruct 转换\n(DTO/VO/Entity)", "#e1d5e7"),
    ("O1", 420, 615, 260, 60, "KnowledgeDocWorkflow\n(工作流定义)", "#e1d5e7"),
    ("O2", 710, 615, 250, 60, "KnowledgeDocActivities\n(活动 + Saga 补偿)", "#e1d5e7"),
    ("O3", 1000,615, 240, 60, "FileExtract / Tika\n(多格式解析)", "#e1d5e7"),
    ("O4", 1280,615, 280, 60, "TextSplitter / Chunking\n(文本切块)", "#e1d5e7"),
    # L5
    ("A1", 140, 750, 300, 80, "ChatClientFactory\n(LLM · OpenAI 兼容)", "#fff2cc"),
    ("A3", 980, 750, 270, 80, "Embedding + Rerank\n(嵌入/重排)", "#fff2cc"),
    ("A2", 1290,750, 270, 80, "VectorServiceImpl\n(Weaviate 读写)", "#fff2cc"),
    # L6
    ("D1", 140, 895, 260, 85, "MySQL 8.4\n(业务关系表)", "#f8cecc"),
    ("D2", 430, 895, 240, 85, "Redis 7.0\n(缓存)", "#f8cecc"),
    ("D4", 710, 895, 240, 85, "MinIO\n(文件对象存储)", "#f8cecc"),
    ("D5", 1000,895, 260, 85, "MongoDB 8.0\n(对话/日志)", "#f8cecc"),
    ("D3", 1290,895, 270, 85, "Weaviate 1.33\n(向量数据库)", "#f8cecc"),
    # L7
    ("I1", 140, 1045,350, 60, "Temporal Server :7233\n(引擎 + UI :8233)", "#d4e1f9"),
    ("I2", 510, 1045,300, 60, "Weaviate Server :8080", "#d4e1f9"),
    ("I3", 830, 1045,300, 60, "docker-compose\n(一键编排)", "#d4e1f9"),
    ("I4", 1150,1045,350, 60, "DailyTask 定时任务\n(调度/同步)", "#d4e1f9"),
]

NODE_D = {n[0]: n for n in NODES}

def anchor(nid, side):
    _, x, y, w, h, *_ = NODE_D[nid]
    if side == "top":    return (x + w/2, y)
    if side == "bottom": return (x + w/2, y + h)
    if side == "left":   return (x, y + h/2)
    if side == "right":  return (x + w, y + h/2)

# edges: id, src, srcmid, tgt, tgtside, label, color, waypoints(list of (x,y))
EDGES = [
    ("e1",  "U1", "right",  "U2", "left",   "浏览器访问",          "#455A64", []),
    ("e2",  "U2", "bottom", "G1", "top",    "REST/JSON + JWT",    "#455A64", [(780,160),(260,160)]),
    ("e3",  "G1", "right",  "G2", "left",   "校验通过",            "#455A64", []),
    ("e4",  "G2", "right",  "G3", "left",   "放行路由",            "#455A64", []),
    ("e5",  "G3", "bottom", "S1", "top",    "CRUD",               "#455A64", []),
    ("e6",  "G3", "bottom", "S2", "top",    "上传/解析",           "#455A64", []),
    ("e7",  "G3", "bottom", "S3", "top",    "回收站",              "#455A64", []),
    ("e8",  "G3", "bottom", "S4", "top",    "RAG 对话",            "#455A64", []),
    ("e9",  "G3", "bottom", "S5", "top",    "模型管理",            "#455A64", []),
    ("e10", "S2", "bottom", "O1", "top",    "上传事件→启动工作流", "#455A64", []),
    ("e11", "O1", "right",  "O2", "left",   "调度 Activity",       "#455A64", []),
    ("e12", "O2", "right",  "O3", "left",   "解析文件",            "#455A64", []),
    ("e13", "O3", "right",  "O4", "left",   "提取文本",            "#455A64", []),
    ("e14", "O4", "bottom", "A2", "top",    "切块→向量化",         "#EF6C00", []),
    ("e15", "A3", "right",  "A2", "left",   "调用嵌入/重排",       "#EF6C00", []),
    ("e16", "A2", "bottom", "D3", "top",    "向量读写",            "#EF6C00", []),
    ("e19", "S9", "bottom", "O3", "top",    "语义切块",            "#455A64", []),
    # cross-cutting storage links via margin channels (no crossings)
    ("e17", "S8", "left",   "D1", "left",   "MyBatis-Plus CRUD",   "#AD1457", [(80,492),(80,937)]),
    ("e18", "S7", "right",  "D4", "right",  "文件流",              "#AD1457", [(1720,492),(1720,937)]),
]

# ---------------------------------------------------------------- drawio
def esc(s): return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
def to_html(label): return label.replace("\n", "<br>")

def build_drawio():
    out = []
    out.append('<?xml version="1.0" encoding="UTF-8"?>')
    out.append(f'<mxGraphModel dx="800" dy="800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="{PAGE_W}" pageHeight="{PAGE_H}" math="0" shadow="0">')
    out.append('  <root>')
    out.append('    <mxCell id="0"/>')
    out.append('    <mxCell id="1" parent="0"/>')
    # title
    out.append(f'    <mxCell id="TITLE" value="{esc("sk-knowledge 企业知识库 · 系统架构图 (Vue3 + SpringBoot + SpringAI + Weaviate + Temporal)")}" style="text;html=1;align=center;verticalAlign=middle;fontSize=20;fontStyle=1;fontColor=#1f4e79;" vertex="1" parent="1"><mxGeometry x="120" y="10" width="1500" height="26" as="geometry"/></mxCell>')
    # layers
    for lid, x, y, w, h, title, fill in LAYERS:
        out.append(f'    <mxCell id="{lid}" value="{esc(title)}" style="rounded=1;whiteSpace=wrap;html=1;fillColor={fill};strokeColor=#999999;strokeWidth=1.5;verticalAlign=top;fontSize=13;fontStyle=1;fontColor=#333333;" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    # nodes
    for nid, x, y, w, h, label, fill in NODES:
        out.append(f'    <mxCell id="{nid}" value="{esc(to_html(label))}" style="rounded=1;whiteSpace=wrap;html=1;fillColor={fill};strokeColor=#5a5a5a;strokeWidth=1.2;fontSize=11;fontStyle=0;verticalAlign=middle;align=center;fontColor=#1a1a1a;" vertex="1" parent="1"><mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
    # legend
    legend = ("图例：  ● 灰色 = 请求/数据流   ● 橙色 = AI/模型调用   ● 品红 = 存储访问(跨层走两侧通道)")
    out.append(f'    <mxCell id="LEGEND" value="{esc(legend)}" style="rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#cccccc;strokeWidth=1;fontSize=10;fontColor=#444444;align=left;verticalAlign=middle;" vertex="1" parent="1"><mxGeometry x="470" y="758" width="490" height="64" as="geometry"/></mxCell>')
    # edges
    for eid, src, sside, tgt, tside, label, color, wps in EDGES:
        sx, sy = anchor(src, sside)
        tx, ty = anchor(tgt, tside)
        pts = ""
        if wps:
            pts = "<Array as=\"points\">" + "".join(f'<mxPoint x="{px}" y="{py}"/>' for px, py in wps) + "</Array>"
        out.append(f'    <mxCell id="{eid}" value="{esc(label)}" style="edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;endArrow=block;strokeColor={color};strokeWidth=2;fontSize=10;fontColor={color};" edge="1" parent="1" source="{src}" target="{tgt}"><mxGeometry relative="1" as="geometry">{pts}</mxGeometry></mxCell>')
    out.append('  </root>')
    out.append('</mxGraphModel>')
    return "\n".join(out)

# ---------------------------------------------------------------- matplotlib png
def build_png(path):
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    from matplotlib.patches import Rectangle, FancyArrowPatch
    import matplotlib.font_manager as fm

    for f in ["Microsoft YaHei", "SimHei", "Arial Unicode MS"]:
        try:
            plt.rcParams["font.sans-serif"] = [f]
            break
        except Exception:
            continue
    plt.rcParams["axes.unicode_minus"] = False

    fig, ax = plt.subplots(figsize=(PAGE_W/100, PAGE_H/100), dpi=100)
    ax.set_xlim(0, PAGE_W); ax.set_ylim(PAGE_H, 0)  # invert y
    ax.axis("off")

    for lid, x, y, w, h, title, fill in LAYERS:
        ax.add_patch(Rectangle((x, y), w, h, fc=fill, ec="#999999", lw=1.5, zorder=0))
        ax.text(x+8, y+14, title, fontsize=11, fontweight="bold", color="#333333", va="top", zorder=1)
    for nid, x, y, w, h, label, fill in NODES:
        ax.add_patch(Rectangle((x, y), w, h, fc=fill, ec="#5a5a5a", lw=1.2, zorder=2))
        ax.text(x+w/2, y+h/2, label, fontsize=8.5, color="#1a1a1a",
                ha="center", va="center", zorder=3, wrap=True)
    # legend box
    ax.add_patch(Rectangle((470, 758), 490, 64, fc="#ffffff", ec="#cccccc", lw=1, zorder=2))
    ax.text(478, 790, "图例：灰=请求/数据流  橙=AI/模型调用  品红=存储访问(跨层走两侧通道)",
            fontsize=8, color="#444444", va="center", zorder=3)

    def draw_edge(src, sside, tgt, tside, label, color, wps):
        sx, sy = anchor(src, sside); tx, ty = anchor(tgt, tside)
        pts = [(sx, sy)] + list(wps) + [(tx, ty)]
        for i in range(len(pts)-1):
            a, b = pts[i], pts[i+1]
            arrow = FancyArrowPatch(a, b, arrowstyle="-|>", mutation_scale=12,
                                    color=color, lw=2, zorder=4)
            ax.add_patch(arrow)
        # label near middle of full polyline
        mx = sum(p[0] for p in pts)/len(pts)
        my = sum(p[1] for p in pts)/len(pts)
        ax.text(mx, my-6, label, fontsize=8, color=color, ha="center", va="bottom", zorder=5)

    for eid, src, sside, tgt, tside, label, color, wps in EDGES:
        draw_edge(src, sside, tgt, tside, label, color, wps)

    fig.savefig(path, dpi=100, bbox_inches="tight", pad_inches=0.2)
    plt.close(fig)

if __name__ == "__main__":
    out_dir = r"D:/Project-self/AI qiyexiangmu"
    with open(out_dir + "/架构图.drawio", "w", encoding="utf-8") as f:
        f.write(build_drawio())
    build_png(out_dir + "/架构图.png")
    print("done")
