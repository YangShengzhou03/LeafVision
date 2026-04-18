export const enToZh = (enObj) => {
  const zhObj = {}
  for (const key in enObj) {
    zhObj[key] = key
  }
  return zhObj
}
